import {
  Main,
  Boarding,
  FoundItemWrite,
  FoundItemDetail,
  AgencyRegist,
  LostItemRegist,
  Boards,
  Introduce,
  AcquireRegist,
  IntroduceDetail,
  Letter,
  Alarm,
  MyPage,
  NaverLogin,
} from "@/pages";
import { Header, Footer } from "@/widgets";
import "./index.css";
import { DarkThemeToggle, Flowbite, Toast } from "flowbite-react";
import {
  BrowserRouter as Router,
  Route,
  Routes,
  Navigate,
} from "react-router-dom";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { useMemberStore } from "@/shared";
import { HelmetProvider } from "react-helmet-async";
import { StateContext, SSEConnect } from "@/shared";
import { useCallback, useEffect, useState } from "react";
import { tokenCheck } from "@/entities";
import LetterRoomDetail from "@/pages/letterRoomDetail";
import { HiExclamation } from "react-icons/hi";
import { FaTelegramPlane } from "react-icons/fa";
import { motion } from "framer-motion";
import { EventSourcePolyfill } from "event-source-polyfill";
const queryClient = new QueryClient();

const App = () => {
  const {
    Authenticate,
    token,
    tokenInitialize,
    memberInitialize,
    authenticateInitialize,
    agencyInitialize,
  } = useMemberStore();
  const [headerTitle, setHeaderTitle] = useState<string>("");
  const [meta, setMeta] = useState(true);
  const [connected, setConnected] = useState(false);
  const [mobile, setMobile] = useState(false);
  const [openToast, setOpenToast] = useState(false);
  const [toastMessage, setToastMessage] = useState("메시지가 도착했어요");

  useEffect(() => {
    // 최초 진입 시 토큰이 있다면 체크, 토큰 유효 시 로그인 처리
    // 토큰이 만료되었을 경우 로그아웃 처리
    if (token.accessToken) {
      tokenCheck(
        () => {
          console.info("유효한 토큰");
        },
        (error) => {
          console.error(error);
          tokenInitialize();
          memberInitialize();
          authenticateInitialize();
          agencyInitialize();
          window.location.href = "/";
        }
      );
      return;
    }
  }, []);

  let eventSource: EventSourcePolyfill;

  const SSEConnection = () => {
    eventSource = SSEConnect();

    eventSource.onopen = (success) => {
      console.log("Server Sent Event 연결이 열렸습니다.", success);
      setConnected(true);
    };

    eventSource.onerror = (error) => {
      console.log("Server Sent Event 오류", error);
      setConnected(false);
    };

    eventSource.onmessage = () => {
      console.log("Server Sent Event 메시지");
    };

    eventSource.addEventListener("message", (event) => {
      const data = JSON.parse(event.data);
      console.log(data);
      setToastMessage(data.content);
      setOpenToast(true);
    });
  };

  useEffect(() => {
    if (!Authenticate || connected) return;
    SSEConnection();

    return () => {
      eventSource.close();
    };
  }, [Authenticate]);

  const checkDevice = useCallback(() => {
    if (window.innerWidth > 1280) {
      setMobile(false);
      return;
    }

    setMobile(true);
  }, []);

  useEffect(() => {
    const handleResize = () => {
      checkDevice();
    };

    window.addEventListener("resize", handleResize);

    return () => {
      window.removeEventListener("resize", handleResize);
    };
  }, []);

  useEffect(() => {
    checkDevice();
  }, []);

  const handleToast = () => {
    return (
      <motion.div
        className="absolute w-[300px] self-center top-[20%] z-[10]"
        initial={{ opacity: 0, y: -100 }}
        animate={{ opacity: 1, y: 0 }}
        exit={{ opacity: 0, y: 100 }}
        transition={{ duration: 0.5 }}
      >
        <Toast>
          <FaTelegramPlane className="h-5 w-5 text-cyan-600 dark:text-cyan-500" />
          <div className="pl-4 text-sm font-normal">{toastMessage}</div>
          <Toast.Toggle onClick={() => setOpenToast(false)} />
        </Toast>
      </motion.div>
    );
  };

  return (
    <HelmetProvider>
      <StateContext.Provider value={{ headerTitle, setHeaderTitle, setMeta }}>
        <Flowbite>
          <QueryClientProvider client={queryClient}>
            <div className="Container">
              <Router>
                <DarkThemeToggle className="absolute right-0 z-10" />
                {meta && <Header />}
                {!mobile && (
                  <Toast className="self-center">
                    <div className="inline-flex h-8 w-8 shrink-0 items-center justify-center rounded-lg bg-orange-100 text-orange-500 dark:bg-orange-700 dark:text-orange-200">
                      <HiExclamation className="h-5 w-5" />
                    </div>
                    <div className="ml-3 text-sm font-normal">
                      Findear 서비스는 모바일 환경에 최적화되어 있습니다. PC
                      환경에서는 일부 기능이 제한되거나 정상 작동하지 않을 수
                      있습니다.
                    </div>
                    <Toast.Toggle />
                  </Toast>
                )}

                {openToast ? handleToast() : ""}
                <main className="flex relative flex-col overflow-y-scroll scrollbar-hide flex-1 xl:mx-[10%]">
                  <Routes>
                    <Route
                      path="/"
                      element={
                        Authenticate ? <Navigate to="/main" /> : <Boarding />
                      }
                    />
                    <Route path="/main" element={<Main />} />
                    <Route
                      path="/foundItemWrite"
                      element={
                        Authenticate ? <FoundItemWrite /> : <Navigate to="/" />
                      }
                    />
                    <Route
                      path="/agencyRegist"
                      element={
                        Authenticate ? <AgencyRegist /> : <Navigate to="/" />
                      }
                    />
                    <Route
                      path="/losts"
                      element={<Boards boardType="분실물" />}
                    />
                    <Route
                      path="/acquire"
                      element={<Boards boardType="습득물" />}
                    />
                    <Route
                      path="/letter/:roomId"
                      element={<LetterRoomDetail />}
                    />
                    <Route path="/introduce" element={<Introduce />} />
                    <Route
                      path="/acquireRegist"
                      element={
                        Authenticate ? <AcquireRegist /> : <Navigate to="/" />
                      }
                    />
                    <Route path="/myPage" element={<MyPage />} />
                    <Route path="/members/login" element={<NaverLogin />} />
                    <Route
                      path="/foundItemDetail/:id"
                      element={
                        <FoundItemDetail />
                        // Authenticate ? (
                        //   <FoundItemDetail />
                        // ) : (
                        //   <Navigate to="/" />
                        // )
                      }
                    />
                    <Route
                      path="/lostItemRegist"
                      element={
                        <LostItemRegist />
                        // Authenticate ? <LostItemRegist /> : <Navigate to="/" />
                      }
                    />
                    <Route
                      path="/introduceDetail"
                      element={<IntroduceDetail />}
                    />
                    <Route
                      path="/letter"
                      element={Authenticate ? <Letter /> : <Navigate to="/" />}
                    />
                    <Route
                      path="/alarm"
                      element={Authenticate ? <Alarm /> : <Navigate to="/" />}
                    />
                  </Routes>
                </main>
                {meta && <Footer />}
              </Router>
            </div>
          </QueryClientProvider>
        </Flowbite>
      </StateContext.Provider>
    </HelmetProvider>
  );
};

export default App;