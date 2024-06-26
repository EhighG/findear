import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { AnimatePresence, motion } from "framer-motion";
import { Label, TextInput, Textarea } from "flowbite-react";
import dayjs from "dayjs";
import { IoCloseSharp } from "react-icons/io5";
import { CustomButton, Text, cls, useMemberStore } from "@/shared";
import { getRoomDetail, roomDetailType, sendMessageInRoom } from "@/entities";

const LetterRoomDetail = () => {
  const roomId = useParams().roomId;
  const [detailData, setDetailData] = useState<roomDetailType>();
  const [openChat, setOpenChat] = useState(false);
  const [title, setTitle] = useState("");
  const [content, setContent] = useState("");
  const { member } = useMemberStore();
  const [acquireTelNum, setAcquireTelNum] = useState<string>("");
  const [trigger, setTrigger] = useState(false);
  const navigate = useNavigate();
  useEffect(() => {
    if (roomId) {
      getRoomDetail(
        parseInt(roomId),
        ({ data }) => {
          setAcquireTelNum(data.result.enquirerTelNum);
          setDetailData(data.result);
        },
        (error) => {
          console.error(error);
        }
      );
    }
  }, [roomId, trigger]);

  const handleBoard = () => {
    navigate(`/foundItemDetail/${detailData?.board.boardId}`, {
      state: { acquireTelNum },
    });
  };

  const sendMessageHandler = () => {
    if (!roomId) return;
    sendMessageInRoom(
      {
        messageRoomId: parseInt(roomId),
        title,
        content,
      },
      () => {
        setTrigger((prev) => !prev);
        setOpenChat(false);
        setTitle("");
        setContent("");
      },
      (error) => {
        console.error(error);
      }
    );
  };

  //  scroll to bottom
  useEffect(() => {
    window.scrollTo(0, document.body.scrollHeight);
  }, [trigger]);

  return (
    <div className="flex flex-col flex-1 relative">
      <AnimatePresence>
        {openChat && (
          <motion.div
            initial={{ y: 800 }}
            animate={{ y: 0 }}
            exit={{ y: 800 }}
            transition={{ ease: "easeOut", duration: 0.3 }}
            className="absolute inset-x-0 inset-y-0 w-full h-full rounded-lg bg-A706LightGrey dark:bg-A706DarkGrey1 z-[10] overflow-x-hidden"
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
                <Label htmlFor="title" value="쪽지 제목" />
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
                placeholder="쪽지 내용을 적어주세요"
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
      <div className="flex w-full h-[80px] border-b border-A706Grey2 p-2">
        <div className="flex flex-1" onClick={() => handleBoard()}>
          <img
            src={detailData?.board.thumbnailUrl ?? ""}
            alt="thumbnail"
            className="inset-0 w-full h-full"
          />
        </div>
        <div className="flex flex-col flex-[4]">
          <div
            className="flex w-full items-center justify-between"
            onClick={() => handleBoard()}
          >
            <Text className="font-bold text-[1.5rem] truncate">
              {detailData ? detailData.board.productName : "물건 제목"}
            </Text>
          </div>
          <div className="flex w-full items-center justify-end">
            <CustomButton
              className="border border-A706Grey2 rounded-md p-1 bg-A706SubBlue"
              onClick={() => {
                setOpenChat(true);
              }}
            >
              <Text className="text-A706LightGrey">쪽지 보내기</Text>
            </CustomButton>
          </div>
        </div>
      </div>

      {!openChat &&
        detailData?.message.map((message) => (
          <div
            key={message.messageId}
            className={cls(
              "chat",
              message.senderId === member.memberId ? "chat-end" : "chat-start",
              "px-2"
            )}
          >
            <div className="chat-header ">
              {message.title}
              <time className="ml-2 text-xs opacity-50">
                {dayjs(message.sendAt).format("YY-MM-DD HH:mm")}
              </time>
            </div>
            <div
              className={cls(
                "chat-bubble",
                message.senderId === member.memberId
                  ? "bg-A706CheryBlue dark:bg-green-500"
                  : "dark:bg-A706DarkGrey1 bg-A706LightGrey2 text-A706DarkGrey2"
              )}
            >
              {message.content}
            </div>
          </div>
        ))}
      <div className="flex flex-1 items-start justify-between">
        <div className="flex flex-col items-center">
          <Text className="text-[1.5rem] font-bold">상대방</Text>

          <img
            src="https://raw.githubusercontent.com/Tarikul-Islam-Anik/Animated-Fluent-Emojis/master/Emojis/People/Man%20Standing.png"
            alt="Detective"
            width="60"
            height="60"
          />
        </div>
        <div className="flex flex-col items-center">
          <Text className="text-[1.5rem] font-bold">나</Text>
          <img
            src="https://raw.githubusercontent.com/Tarikul-Islam-Anik/Animated-Fluent-Emojis/master/Emojis/People/Detective.png"
            alt="Detective"
            width="60"
            height="60"
          />
        </div>
      </div>
    </div>
  );
};

export default LetterRoomDetail;
