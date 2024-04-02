import { useEffect, useState } from "react";
import {
  useLocation,
  useNavigate,
  useParams,
  useSearchParams,
} from "react-router-dom";
import {
  Carousel,
  FloatingLabel,
  Label,
  Modal,
  TextInput,
  Textarea,
  Tooltip,
} from "flowbite-react";
import { AnimatePresence, motion } from "framer-motion";
import SendOutlinedIcon from "@mui/icons-material/SendOutlined";
import FavoriteIcon from "@mui/icons-material/Favorite";
import FavoriteBorderOutlinedIcon from "@mui/icons-material/FavoriteBorderOutlined";
import { IoCloseSharp } from "react-icons/io5";
import { deleteAcquisitions, deleteLosts, receiverType } from "@/entities";
import {
  CustomButton,
  KakaoMap,
  Text,
  useDebounce,
  useMemberStore,
  usePhoneValidation,
  useSearchMap,
} from "@/shared";
import {
  Lost112ListType,
  checkPhone,
  getAcquisitionsDetail,
  sendMessage,
} from "@/entities";
import {
  cancelScarppedBoard,
  scrapBoard,
  returnAcquisitions,
  infoType,
} from "@/entities";
import { BsThreeDotsVertical } from "react-icons/bs";
import { IoIosArrowBack } from "react-icons/io";
import { ListGroup } from "flowbite-react";
import Swal from "sweetalert2";
const foundItemDetail = () => {
  const navigate = useNavigate();
  const { member } = useMemberStore();

  const location = useLocation();
  const acquireTelNum = location.state?.acquireTelNum ?? "";
  const state = location.state as Lost112ListType;
  useEffect(() => {
    if (!state || acquireTelNum) return;
    console.log(state);
    setIsFindear(false);

    fetchData();
  }, []);

  const fetchData = async () => {
    const keyword = state.depPlace;
    if (!keyword) return;

    const searchData = await useSearchMap(keyword);
    console.log("searchData", searchData);
    if (searchData) {
      setPhone(searchData[0].phone);
      return;
    }
    setPhone("01012345678");
  };

  const [detailData, setDetailData] = useState<infoType>();

  const [isScrapped, setScrapped] = useState<boolean>(false);
  const [isReturned, setReturned] = useState<boolean>(false);
  const [openChat, setOpenChat] = useState<boolean>(false);
  const boardId = parseInt(useParams().id ?? "0");
  const [title, setTitle] = useState<string>("");
  const [content, setContent] = useState<string>("");
  const [query] = useSearchParams();
  const [receiver, setReceiver] = useState<receiverType>();
  const [receiverPhoneNumber, setReceiverPhoneNumber] = useState<string>("");
  const [isFindear, setIsFindear] = useState<boolean>(true);
  const [phone, setPhone] = useState<string>("");
  const [step, setStep] = useState<number>(0);
  const [isMember, setIsMember] = useState<boolean>(false);
  const [modalOptions, setModalOptions] = useState<boolean>(false);
  const [userExist, setUserExist] = useState<boolean>(false);
  const [openOption, setOpenOption] = useState<boolean>(false);
  const debouncedPhoneNumber = useDebounce(receiverPhoneNumber, 500);

  const initReceiver = () => {
    if (query) {
      setReceiver({
        phoneNumber: query.get("phoneNumber") ?? "",
      });
    }
  };

  useEffect(() => {
    initReceiver();
  }, [query]);

  useEffect(() => {
    if (acquireTelNum) {
      setReceiverPhoneNumber(acquireTelNum);
      setStep(1);
      setIsMember(true);
    }
  }, []);

  useEffect(() => {
    if (!isFindear) return;
    getAcquisitionsDetail(
      boardId,
      ({ data }) => {
        console.log(data);
        setDetailData(data.result);
      },
      (error) => console.log(error)
    );
  }, [isFindear]);

  const returnItem = () => {
    if (receiver) {
      returnAcquisitions(
        { boardId, receiver },
        ({ data }) => {
          console.log(data);
          alert(data.message);
          setReturned(true);
        },
        (error) => {
          alert(error.message);
          setReturned(false);
        }
      );
      setModalOptions(false);
    } else {
      alert("인계 대상자 덜 참");
    }
  };

  const sendMessageHandler = () => {
    sendMessage(
      {
        boardId,
        title,
        content,
        sender: member.memberId,
      },
      () => {
        navigate("/letter");
      },
      (error) => {
        console.log(error);
      }
    );
  };

  const scarpItem = () => {
    if (!isScrapped) {
      scrapBoard(
        boardId,
        () => {
          setScrapped(true);
        },
        (error) => {
          console.log(error);
          alert(error.message);
        }
      );
      return;
    }
    cancelScarppedBoard(
      boardId,
      () => {
        setScrapped(false);
      },
      (error) => {
        console.log(error);
        alert(error.message);
      }
    );
  };

  const cancleReturn = () => {
    alert("인계가 취소되었습니다.");
    setModalOptions(false);
    setStep(0);
    setIsMember(false);
  };

  const handleDelete = () => {
    if (isFindear) {
      deleteAcquisitions(
        boardId,
        () => {
          Swal.fire({
            title: "삭제 완료",
            icon: "success",
            text: "게시글이 성공적으로 삭제되었습니다.",
          }).then(() => {
            navigate(-1);
          });
        },

        () => {
          Swal.fire({
            title: "삭제 실패",
            icon: "error",
            text: "게시글 삭제가 실패되었습니다.",
          });
        }
      );
    } else {
      deleteLosts(
        boardId,
        () => {
          Swal.fire({
            title: "삭제 완료",
            icon: "success",
            text: "게시글이 성공적으로 삭제되었습니다.",
          }).then(() => {
            navigate(-1);
          });
        },
        () => {
          Swal.fire({
            title: "삭제 실패",
            icon: "error",
            text: "게시글 삭제가 실패되었습니다.",
          });
        }
      );
    }
  };

  const handleReturn = () => {
    if (confirm("해당 회원에게 습득물을 인계 하시겠습니까?")) {
      returnItem();
      return;
    }
    cancleReturn();
  };

  useEffect(() => {
    if (debouncedPhoneNumber.length > 10) {
      checkPhone(
        debouncedPhoneNumber,
        ({ data }) => {
          if (data.result) {
            setUserExist(true);
            return;
          }
          setUserExist(false);
        },
        (error) => {
          console.log(error);
        }
      );
    }
  }, [debouncedPhoneNumber]);

  useEffect(() => {
    if (receiverPhoneNumber) {
      setReceiver({
        phoneNumber: receiverPhoneNumber,
      });
    }
  }, [receiverPhoneNumber]);

  return (
    <div className="flex flex-col flex-1 justify-center items-center p-[20px] relative">
      <div className="flex w-full justify-between my-2">
        <CustomButton onClick={() => navigate(-1)}>
          <IoIosArrowBack size="30px" />
        </CustomButton>
        {member.memberId === detailData?.board.member.memberId && (
          <div className="flex relative">
            {openOption && (
              <ListGroup className="absolute min-w-[100px] right-8 top-0 z-[10]">
                <ListGroup.Item
                  onClick={() =>
                    Swal.fire({
                      title: "게시글 삭제",
                      icon: "warning",
                      html: `삭제된 게시글은 복구할 수 없습니다. <br/> 삭제하시겠습니까?`,
                      showCancelButton: true,
                    }).then((result) => {
                      if (result.isConfirmed) {
                        handleDelete();
                      }
                    })
                  }
                >
                  삭제하기
                </ListGroup.Item>
                <ListGroup.Item onClick={() => alert("수정할까?")}>
                  수정하기
                </ListGroup.Item>
              </ListGroup>
            )}
            <CustomButton>
              <BsThreeDotsVertical
                size="30px"
                onClick={() => setOpenOption((prev) => !prev)}
              />
            </CustomButton>
          </div>
        )}
      </div>

      <AnimatePresence>
        {openChat && (
          <motion.div
            initial={{ y: 800 }}
            animate={{ y: 0 }}
            exit={{ y: 800 }}
            transition={{ ease: "easeOut", duration: 0.3 }}
            className="absolute inset-x-0 inset-y-0 w-full h-full rounded-lg bg-A706LightGrey z-[10] overflow-x-hidden"
          >
            <div className="flex items-center justify-between px-[10px]">
              <Text className="text-[1.5rem] font-bold p-[10px]">
                쪽지 보내기
              </Text>
              <div
                onClick={() => {
                  setOpenChat(false);
                }}
              >
                <IoCloseSharp size="32" />
              </div>
            </div>
            <div className="p-5">
              <div className="pb-2 block">
                <Label htmlFor="title" color="success" value="쪽지 제목" />
              </div>
              <TextInput
                id="title"
                placeholder="쪽지 제목을 입력해주세요"
                autoComplete="off"
                required
                value={title}
                onChange={(e) => setTitle(e.target.value)}
              />
            </div>
            <div className="p-5">
              <div className="pb-2 block">
                <Label htmlFor="content" value="쪽지 내용" />
              </div>
              <Textarea
                id="content"
                placeholder="쪽지 내용을 적어주세요, 습득물을 보관중인 관리자 분께 보내는 쪽지입니다."
                required
                rows={10}
                value={content}
                onChange={(e) => setContent(e.target.value)}
              />
            </div>
            <div className="flex justify-center p-5">
              <CustomButton
                className="menubtn"
                onClick={() => sendMessageHandler()}
              >
                쪽지 전송
              </CustomButton>
            </div>
          </motion.div>
        )}
      </AnimatePresence>
      <div className="flex flex-row justify-between w-[340px]">
        <span className="bg-A706Blue2 text-A706CheryBlue text-xs font-bold me-2 px-2.5 py-0.5 rounded dark:bg-blue-900 dark:text-blue-300">
          {isFindear
            ? detailData?.board.categoryName ?? "카테고리 없음"
            : state.mainPrdtClNm ?? "카테고리 없음"}
        </span>
        <Text className="text-md font-bold">
          보관장소 :
          {isFindear
            ? detailData?.agencyName ?? "시설명"
            : state?.depPlace ?? "시설명"}
        </Text>
      </div>
      <div className="flex flex-col justify-center p-[40px] gap-[20px]">
        <div className="flex  items-center justify-center size-[300px]">
          <Carousel>
            {isFindear ? (
              detailData?.board.imgUrls.map((imgUrl, idx) => (
                <div
                  className="flex justify-center w-full h-full border border-A706Grey2 rounded-xl"
                  key={idx}
                >
                  <img
                    src={imgUrl}
                    alt="이미지가 없습니다."
                    className="object-fill w-full h-full rounded-xl"
                  />
                </div>
              ))
            ) : (
              <div
                className="flex justify-center w-full h-full border border-A706Grey2 rounded-xl"
                key={state.atcId}
              >
                <img
                  src={state.fdFilePathImg}
                  alt="이미지가 없습니다."
                  className="object-fill rounded-xl"
                />
              </div>
            )}
          </Carousel>
        </div>
      </div>
      <div className="w-[340px] flex flex-col text-center">
        <Text className="text-md">
          {isFindear
            ? detailData?.address + ", " + detailData?.agencyName
            : state.depPlace}
        </Text>
        <p className="text-md font-bold">
          {isFindear ? detailData?.board.registeredAt : state.fdYmd}
        </p>
      </div>
      <div className="flex flex-row justify-between mt-10 w-[340px]">
        <div className="w-full">
          <Label color="secondary" value="물품명" />
          <Text className="text-lg font-bold">
            {isFindear
              ? detailData?.board.productName ?? "물품명"
              : state.fdPrdtNm}
          </Text>
          <div className="h-[1px] bg-A706DarkGrey2"></div>
        </div>
      </div>
      <KakaoMap
        className="size-[340px] mt-[20px]"
        keyword={isFindear ? detailData?.agencyName : state.depPlace}
      />
      {/* <div className="w-[340px] mt-10">
          <Label color="secondary" value="설명" />
          <div className="bg-white border-2 min-h-[50px] p-5 rounded-md">
            {detailData?.board.description ?? "설명이 없습니다."}
          </div>
        </div> */}
      <div className="w-[340px] mt-10 flex flex-row justify-around">
        {isFindear ? (
          member.memberId !== detailData?.board.member.memberId ? (
            <>
              <CustomButton
                className="rounded-md bg-A706CheryBlue text-white text-sm w-full flex flex-row justify-around p-5 m-3"
                onClick={() => {
                  setOpenChat(true);
                }}
              >
                <>
                  <SendOutlinedIcon className="self-center" />
                  <p className="self-center">쪽지 보내기</p>
                </>
              </CustomButton>
              {isScrapped ? (
                <CustomButton
                  className="rounded-md bg-A706Red text-white text-sm w-full flex flex-row justify-around p-5 m-3"
                  onClick={() => scarpItem()}
                >
                  <>
                    <FavoriteIcon />
                    <p>스크랩 완료</p>
                  </>
                </CustomButton>
              ) : (
                <CustomButton
                  className="rounded-md bg-A706Grey2 text-white text-sm w-full flex flex-row justify-around p-5 m-3"
                  onClick={() => {
                    scarpItem();
                  }}
                >
                  <>
                    <FavoriteBorderOutlinedIcon />
                    <p>스크랩하기</p>
                  </>
                </CustomButton>
              )}
            </>
          ) : (
            <>
              {isReturned ? (
                <CustomButton className="rounded-md w-[320px] h-[60px] bg-A706DarkGrey1 text-white">
                  <p>인계 완료</p>
                </CustomButton>
              ) : (
                <CustomButton
                  className="rounded-md bg-A706CheryBlue text-white text-base w-full p-3"
                  onClick={() => setModalOptions(true)}
                >
                  <p>인계하기</p>
                </CustomButton>
              )}
            </>
          )
        ) : (
          <CustomButton
            className="rounded-md bg-A706CheryBlue text-white text-base w-full p-3"
            onClick={() => {
              alert("Lost112에 등록된 습득물은 전화로만 연락 가능합니다.");
              window.location.href = `tel:${phone}`;
            }}
          >
            <p>전화로 연락</p>
          </CustomButton>
        )}
      </div>

      <Modal
        dismissible
        show={modalOptions}
        position={"center"}
        size="md"
        onClose={() => {
          setModalOptions(false);
        }}
      >
        <Modal.Header>
          <p>인계 하기</p>
        </Modal.Header>
        <Modal.Body className="flex flex-col justify-center">
          {step === 0 && (
            <div className="flex flex-col w-sm">
              <div className="flex gap-[10px]">
                <Text>인계자가 Findear 회원인가요? </Text>
                <Tooltip content="인계자가 Findear 회원이라면 간단 인적사항만 확인하시고 걱정없이 인계 하세요, Findear는 100% 본인인증이 된 회원들만 이용할 수 있어요">
                  <img
                    src="https://raw.githubusercontent.com/Tarikul-Islam-Anik/Animated-Fluent-Emojis/master/Emojis/Symbols/Information.png"
                    alt="Information"
                    width="25"
                    height="25"
                  />
                </Tooltip>
              </div>
              <div className="flex gap-[10px]">
                <CustomButton
                  className="rounded-md w-full bg-A706CheryBlue text-white text-base p-3 mt-5 self-center"
                  onClick={() => {
                    setIsMember(true);
                    setStep(1);
                  }}
                >
                  <p className="flex justify-center items-center gap-[10px]">
                    네
                    <img
                      src="https://raw.githubusercontent.com/Tarikul-Islam-Anik/Animated-Fluent-Emojis/master/Emojis/Smilies/Beaming%20Face%20with%20Smiling%20Eyes.png"
                      alt="Beaming Face with Smiling Eyes"
                      width="25"
                      height="25"
                    />
                  </p>
                </CustomButton>
                <CustomButton
                  className="rounded-md w-full bg-A706Grey2 text-white text-base p-3 mt-5 self-center"
                  onClick={() => {
                    setIsMember(false);
                    setStep((prev) => prev + 1);
                  }}
                >
                  <p className="flex justify-center items-center gap-[10px]">
                    아니요{" "}
                    <img
                      src="https://raw.githubusercontent.com/Tarikul-Islam-Anik/Animated-Fluent-Emojis/master/Emojis/Smilies/Disappointed%20Face.png"
                      alt="Disappointed Face"
                      width="25"
                      height="25"
                    />
                  </p>
                </CustomButton>
              </div>
            </div>
          )}
          {step === 1 && (
            <>
              <Text>
                {isMember
                  ? "전화번호 입력후 Findear 회원 인증여부를 확인하세요, 입력창 아래에 인증여부가 표시됩니다."
                  : "인계자가 Findear 회원이 아니라면 연락처만 입력해 주세요."}
              </Text>
              <FloatingLabel
                variant="outlined"
                label="인계할 사용자 번호를 입력해 주세요."
                sizing="sm"
                value={receiverPhoneNumber}
                onChange={(e) =>
                  setReceiverPhoneNumber(usePhoneValidation(e.target.value))
                }
                helperText={
                  userExist
                    ? "Findear에 가입된 인증 회원입니다"
                    : "가입된 회원이면 인증 회원이라 표시 됩니다."
                }
              />
              <CustomButton
                className="rounded-md w-full bg-A706CheryBlue text-white text-base p-3 mt-5 self-center"
                onClick={() => handleReturn()}
              >
                <p>인계</p>
              </CustomButton>
              <CustomButton
                className="rounded-md w-full bg-A706Red text-white text-base p-3 mt-5 self-center"
                onClick={() => cancleReturn()}
              >
                <p>인계 취소</p>
              </CustomButton>
            </>
          )}
        </Modal.Body>
      </Modal>
    </div>
  );
};

export default foundItemDetail;