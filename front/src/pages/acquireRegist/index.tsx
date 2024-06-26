import React, { useRef, useState, useEffect, useContext } from "react";
import { useNavigate } from "react-router-dom";
import { Helmet } from "react-helmet-async";
import { Modal, Label, TextInput } from "flowbite-react";
import Swal from "sweetalert2";
import AWS from "aws-sdk";
import {
  CustomButton,
  StateContext,
  Text,
  useLongPress,
  useGenerateHexCode,
} from "@/shared";
import { registAcquisitions } from "@/entities";

const AcquireRegist = () => {
  const navigate = useNavigate();
  const [openModal, setOpenModal] = useState(false);
  const [images, setImages] = useState<File[]>([]);
  const [imgUrls, setImgUrls] = useState<string[]>([]);
  const [modalTitle, setModalTitle] = useState("");
  const [modalBody, setModalBody] = useState("");
  const imageRef = useRef<HTMLInputElement>(null);
  const [modalImage, setModalImage] = useState<string>("");
  const [productName, setProductName] = useState("");
  const [deleteMode, setDeleteMode] = useState(false);
  const { setHeaderTitle } = useContext(StateContext);

  const handleLongPress = () => {
    setDeleteMode(true);
  };

  const { onMouseDown, onMouseUp, onMouseLeave, onTouchStart, onTouchEnd } =
    useLongPress(handleLongPress);

  AWS.config.update({
    accessKeyId: import.meta.env.VITE_S3_ACCESS_KEY,
    secretAccessKey: import.meta.env.VITE_S3_SECRET_ACCESS_KEY,
  });

  const myBucket = new AWS.S3({
    params: { Bucket: "findearbucket" },
    region: "ap-northeast-2",
  });

  const uploadFile = async () => {
    try {
      const uploadPromises = images.map((image) => {
        return new Promise((resolve, reject) => {
          const imageName = useGenerateHexCode(image.name);

          const params = {
            Body: image,
            Bucket: "findearbucket",
            Key: imageName,
          };

          myBucket.putObject(params, (err, data) => {
            if (err) {
              setImgUrls([]);
              setImages([]);
              reject(err);
            } else {
              const url = myBucket.getSignedUrl("getObject", {
                Key: params.Key,
              });
              const imgUrl = url.split("?")[0];
              imgUrls.push(imgUrl);
              resolve(data);
            }
          });
        });
      });

      // 모든 이미지 업로드가 완료될 때까지 기다림
      await Promise.all(uploadPromises).then(() => {
        registItem();
      });

      // 이미지 업로드가 완료되면 registItem 함수 호출
    } catch (error) {
      console.error("이미지 업로드 중에 오류가 발생했습니다.", error);
    }
  };

  const registItem = async () => {
    if (imgUrls.length === 0) return;
    await registAcquisitions(
      {
        imgUrls,
        productName,
      },
      () => {
        Swal.fire({
          icon: "success",
          text: "정상적으로 등록되었습니다.",
        });
        navigate("/acquire");
      },
      (error) => {
        Swal.fire({
          icon: "error",
          title: "등록 중 오류가 발생했습니다.",
          text: error.response.data.message,
        });
      }
    );
  };

  const handleUpload = async () => {
    uploadFile(); // AWS S3에 이미지 업로드
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (!e.target.files) return;

    if (e.target.files.length > 6) {
      setModalTitle("알림");
      setModalBody("이미지는 최대 6개까지 업로드 가능합니다.");
      setOpenModal(true);
      return;
    }

    setImages((prevImages) => {
      return [...prevImages, ...(e.target.files || [])];
    });
  };

  useEffect(() => {
    setHeaderTitle("습득물 등록");
    return () => setHeaderTitle("");
  }, []);

  return (
    <div
      className="flex flex-col mx-[10px]"
      onClick={() => setDeleteMode(false)}
    >
      <Helmet>
        <title>습득 물건 등록</title>
        <meta name="description" content="습득 물건 등록 페이지" />
        <meta name="keywords" content="Findear, Lost, items, 습득, 등록" />
      </Helmet>
      <Modal
        show={openModal}
        onClose={() => {
          setOpenModal(false);
          setModalImage("");
          setModalTitle("");
          setModalBody("");
        }}
      >
        <Modal.Header>{modalTitle}</Modal.Header>
        <Modal.Body>
          <div className="space-y-6">
            {modalBody ? (
              <Text>{modalBody}</Text>
            ) : (
              <img src={modalImage} alt={modalImage} className="object-fill" />
            )}
          </div>
        </Modal.Body>
      </Modal>
      <div className="flex flex-col m-3 h-[350px] p-2 rounded-lg bg-A706LightGrey  shadow-lg">
        {images.length === 0 ? (
          <Text className="text-center font-bold text-[1.3rem]">
            습득물 사진을 1장 이상 올려주세요
          </Text>
        ) : (
          ""
        )}
        <div className="flex flex-wrap gap-[10px] h-full">
          {images.length < 6 ? (
            <div>
              <form className="flex flex-col size-[100px]">
                <label htmlFor="image">
                  <div className="flex flex-col items-center justify-center border border-A706Grey rounded-lg">
                    <img
                      src="https://raw.githubusercontent.com/Tarikul-Islam-Anik/Animated-Fluent-Emojis/master/Emojis/Objects/Camera.png"
                      alt="Camera"
                      width="70"
                      height="70"
                    />
                    <Text className="text-[1rem] font-bold">
                      {images.length.toString()}/6
                    </Text>
                  </div>
                  <input
                    id="image"
                    ref={imageRef}
                    title="image"
                    type="file"
                    multiple
                    className="hidden"
                    onChange={handleChange}
                  ></input>
                </label>
              </form>
            </div>
          ) : (
            ""
          )}

          {images.map((image, index) => {
            return (
              <div
                key={index}
                className="flex flex-col size-[100px] items-center justify-center border rounded-xl relative"
                onMouseDown={onMouseDown}
                onMouseUp={onMouseUp}
                onMouseLeave={onMouseLeave}
                onTouchStart={onTouchStart}
                onTouchEnd={onTouchEnd}
              >
                {deleteMode ? (
                  <CustomButton
                    className="btn btn-square absolute -right-4 -top-4"
                    onClick={() => {
                      setImages((prevImages) =>
                        prevImages.filter((_, i) => i !== index)
                      );
                    }}
                  >
                    <svg
                      xmlns="http://www.w3.org/2000/svg"
                      className="h-6 w-6"
                      fill="none"
                      viewBox="0 0 24 24"
                      stroke="currentColor"
                    >
                      <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        strokeWidth="2"
                        d="M6 18L18 6M6 6l12 12"
                      />
                    </svg>
                  </CustomButton>
                ) : (
                  ""
                )}
                <img
                  src={URL.createObjectURL(image)}
                  alt="image"
                  className="object-fill w-full h-full rounded-xl"
                  onClick={() => {
                    setModalTitle(image.name);
                    setModalImage(URL.createObjectURL(image));
                    setOpenModal(true);
                  }}
                />
              </div>
            );
          })}
          {images.length > 0 ? (
            <Text className="mx-auto font-bold text-[1.2rem]">
              사진을 길게 누르면 삭제 가능해요
            </Text>
          ) : (
            ""
          )}
        </div>
      </div>
      <div className="flex flex-col items-center mx-[10px] mt-[20px] gap-[10px]">
        {images.length > 0 ? (
          <Label
            htmlFor="item"
            className="w-full max-w-sm text-[1rem] font-bold"
          >
            습득물 명
            <TextInput
              id="item"
              placeholder="습득물을 적어주세요 ex) 갤럭시, 아이폰, 검정 지갑"
              value={productName}
              onChange={(e) => setProductName(e.target.value)}
              className="w-full max-w-sm CheryBlue border-2 rounded-lg dark:border-A706LightGrey border-A706DarkGrey2"
              required
              helperText="습득물 명만 적으시면 AI가 알아서 분류해요!"
            />
          </Label>
        ) : (
          ""
        )}
        <CustomButton
          className="menubtn my-[10px]"
          disabled={images.length === 0 || productName === ""}
          onClick={handleUpload}
        >
          습득물 등록
        </CustomButton>
      </div>
    </div>
  );
};

export default AcquireRegist;
