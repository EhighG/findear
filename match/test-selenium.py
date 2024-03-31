from selenium import webdriver 
from webdriver_manager.chrome import ChromeDriverManager # 크롬 드라이버 설치 
from selenium.webdriver.chrome.service import Service # 자동적 접근
from selenium.webdriver.chrome.options import Options # 크롭 드라이버 옵션 지정
from selenium.webdriver.common.by import By # find_element 함수 쉽게 쓰기 위함
from selenium.webdriver.common.keys import Keys



path = 'http://web.kats.go.kr/KoreaColor/color.asp'

driver = webdriver.Chrome()
driver.get(path)
print(driver)
print(driver.name)
title = driver.title
print(title)

#driver.get("https://www.selenium.dev/selenium/web/web-form.html")

#title = driver.title

#driver.implicitly_wait(0.5)

#text_box = driver.find_element(by=By.NAME, value="my-text")
#submit_button = driver.find_element(by=By.CSS_SELECTOR, value="button")

#text_box.send_keys("Selenium")
#submit_button.click()

#message = driver.find_element(by=By.ID, value="message")
#text = message.text

#driver.quit()