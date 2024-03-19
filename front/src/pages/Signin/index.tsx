import { useState } from "react";
import { Link } from "react-router-dom";
import { CustomButton, Text } from "@/shared";
import { Label, TextInput } from "flowbite-react";
import { HiMail } from "react-icons/hi";
import { signIn } from "@/entities";
import { useMemberStore } from "@/shared";

const Signin = () => {
  const [phoneNumber, setPhoneNumber] = useState<string>("");
  const [password, setPassword] = useState<string>("");
  const { setToken } = useMemberStore();

  const handleLogin = (phoneNumber: string, password: string) => {
    signIn(
      { phoneNumber, password },
      ({ data }) => {
        console.log(data);
        setToken({
          accessToken: data.result.accessToken,
          refreshToken: data.result.refreshToken,
        });
        // window.location.href = "/";
      },
      (error) => {
        console.log(error);
      }
    );
  };
  return (
    <div className="flex flex-col w-full h-full justify-center items-center">
      <div className="flex flex-col w-full gap-[15px] items-center">
        <Text className="text-center text-4xl">로그인</Text>
        <div className="w-[340px]">
          <div className="mb-2 block">
            <Label htmlFor="phoneNumber" color="success" value="이메일" />
          </div>
          <div className="flex w-full"></div>
          <TextInput
            id="phoneNumber"
            type="phoneNumber"
            icon={HiMail}
            placeholder="FindHere@findear.com"
            required
            onChange={(e) => setPhoneNumber(e.target.value)}
          />
        </div>
        <div className="w-[340px]">
          <div className="mb-2 block">
            <Label htmlFor="password" color="success" value="비밀번호" />
          </div>
          <TextInput
            id="password"
            type="password"
            placeholder="비밀번호"
            required
            onChange={(e) => setPassword(e.target.value)}
          />
        </div>
        <div className="flex gap-[5px] items-center justify-center mt-[20px]">
          <Text className="faint text-[1rem] font-bold ">
            비밀번호를 잊으셨나요?
          </Text>
          <Text className="text-A706SubBlue text-[1.5rem] font-bold cursor-pointer">
            <Link to="/findpassword">비밀번호 찾기</Link>
          </Text>
        </div>
      </div>
      <div className="flex flex-col items-center mt-[40px]">
        <CustomButton
          className="menubtn mt-[20px]"
          onClick={() => handleLogin(phoneNumber, password)}
        >
          로그인
        </CustomButton>
        <div className="flex gap-[5px] items-center justify-center mt-[20px]">
          <Text className="text-[2rem] ">계정이 없다면?</Text>
          <Text className="text-A706CheryBlue text-[2rem] font-bold cursor-pointer">
            <Link to="/main">둘러보기</Link>
          </Text>
        </div>
      </div>
    </div>
  );
};

export default Signin;
