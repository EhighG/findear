from selenium import webdriver 
from selenium.webdriver.common.by import By # find_element 함수 쉽게 쓰기 위함
from selenium.webdriver.common.action_chains import ActionChains
from selenium.webdriver.common.keys import Keys
from selenium.webdriver.support.ui import Select
import random

path = 'http://web.kats.go.kr/KoreaColor/color.asp'
query = '노랑'
timeToWait = 0.001

driver = webdriver.Chrome()
driver.get(path)

inputBox = driver.find_element(By.XPATH,'/html/body/form/table/tbody/tr[2]/td/table/tbody/tr/td/table/tbody/tr[1]/td[1]/table/tbody/tr/td[3]/table/tbody/tr[3]/td/table/tbody/tr/td[2]/input' )
inputBox.clear()
inputBox.send_keys(query)
inputBox.send_keys(Keys.RETURN)
driver.implicitly_wait(timeToWait)

colorBox = driver.find_element(By.XPATH, '/html/body/form/table/tbody/tr[2]/td/table/tbody/tr/td/table/tbody/tr[1]/td[1]/table/tbody/tr/td[3]/table/tbody/tr[6]/td/div/select')
colorLst = colorBox.text.split('\n')
if query in colorLst:
    print('exact color exists')
    index = colorLst.index(query)
else:
    index = random.randint(0, len(colorLst))
print(index)

se = Select(colorBox)
se.select_by_index(index)
selected_option = se.first_selected_option
# ActionChains 객체 생성 후 더블 클릭 액션 추가
action = ActionChains(driver)
action.double_click(selected_option).perform()
driver.implicitly_wait(timeToWait)

# 빈문자열일 때 고려.
# 값이 하나면 그것으로 고정
rgb = driver.find_element(By.NAME, 'html_text').get_attribute('value')
print(rgb)
