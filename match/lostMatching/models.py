from django.db import models

import pandas as pd
import numpy as np
import fasttext
from sklearn.preprocessing import MinMaxScaler
import haversine.haversine as hv
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity
from kiwipiepy import Kiwi
import re
from dotenv import load_dotenv
import os
import time
import logging

from selenium import webdriver 
from selenium.webdriver.common.by import By # find_element 함수 쉽게 쓰기 위함
from selenium.webdriver.common.action_chains import ActionChains
from selenium.webdriver.common.keys import Keys
from selenium.webdriver.support.ui import Select
import random
# Create your models here.

logger = logging.getLogger(__name__)
class matchModel():
    
    def __init__(self) -> None:
        # load model
        load_dotenv()
        path = os.getenv("MODEL_PATH")
        #model = fasttext.load_model(path)

        # open color crawling
        self.webPath = 'http://web.kats.go.kr/KoreaColor/color.asp'
        self.driver = webdriver.Chrome()
        self.driver.get(self.webPath)
        print('driver', self.driver)
        self.timeToWait = 0.001

        return None

    def setData(self, lost, found):
        self.lost = lost
        self.found = found
        self.score = pd.DataFrame()
        logger.debug(f'Get Data')
        logger.debug(f'lost :: {lost}')
        logger.debug(f'found :: {found}')

    def preprocess(self, source):
        if source not in ['lost112', 'findear']: 
            raise Exception('input wrong')
        self.source = source 
        # set lost item type
        self.lost['lostBoardId'] = int(self.lost['lostBoardId'])
        self.lost['xpos'] = float(self.lost['xpos'])
        self.lost['ypos'] = float(self.lost['ypos'])

        # set found item type
        self.found = pd.DataFrame(self.found)
        self.found['acquiredBoardId'] = self.found['acquiredBoardId'].astype(int)

        if self.source == 'findear':
            self.found['xpos'] = self.found['xpos'].astype(float)
            self.found['ypos'] = self.found['ypos'].astype(float)
        elif self.source == 'lost112':
            pass

    def calColor(self):
        self.score['color'] = 0
        lostColor = self.getColor(self.lost['color'])
        if lostColor is None: return None
        lostRGB = self.getCodeFromColor(lostColor)
        npLst = np.array([])
        self.found['color'] = ['레드', '블루', '그린']
        for i in self.found['color']:
            foundColor = self.getColor(i)
            print(foundColor)
            if foundColor is None:
                npLst = np.append(npLst,[255])
                continue
            foundRGB = self.getCodeFromColor(foundColor)
            for w,l,f in zip([0.3,0.59,0.11], lostRGB, foundRGB):
                print(w, l, f)
                print(w*((l-f)**2))
            colorScore = np.sqrt(sum([ w*((l-f)**2) for w,l,f in zip([0.3,0.59,0.11], lostRGB, foundRGB)]))
            print(colorScore)
            npLst = np.append(npLst,[colorScore])
        print('complete np', npLst )
        npLst = npLst.reshape(-1,1)
        minmaxScaler = MinMaxScaler().fit([[0],[255]])
        X_train_minmax = minmaxScaler.transform(npLst)
        nplst = 1 - np.array(X_train_minmax).squeeze()

        self.score['color'] = nplst
        return
    
    def cal(self):
        pass
    

    def test(self):
        print('teststest')
        return 'test'

    def getColor(self, query):
        '''
        None 반환 시 색 계산 제외
        '''
        if query == '':
            return None
        elif len(query) == 1:
            query = query+'색' 
        inputBox = self.driver.find_element(By.XPATH,'/html/body/form/table/tbody/tr[2]/td/table/tbody/tr/td/table/tbody/tr[1]/td[1]/table/tbody/tr/td[3]/table/tbody/tr[3]/td/table/tbody/tr/td[2]/input' )
        inputBox.clear()
        inputBox.send_keys(query)
        inputBox.send_keys(Keys.RETURN)
        self.driver.implicitly_wait(self.timeToWait)

        colorBox = self.driver.find_element(By.XPATH, '/html/body/form/table/tbody/tr[2]/td/table/tbody/tr/td/table/tbody/tr[1]/td[1]/table/tbody/tr/td[3]/table/tbody/tr[6]/td/div/select')
        colorLst = colorBox.text.split('\n')
        if len(colorLst) == 1:
            index = 0
        elif len(colorLst) == 0:
            return None
        elif query in colorLst:
            index = colorLst.index(query)
        else:
            index = random.randint(0, len(colorLst))

        select = Select(colorBox)
        select.select_by_index(index)
        selected_option = select.first_selected_option
        action = ActionChains(self.driver)
        action.double_click(selected_option).perform()
        self.driver.implicitly_wait(self.timeToWait)

        rgb = self.driver.find_element(By.NAME, 'html_text').get_attribute('value')
        return rgb
    
    def getCodeFromColor(self, color):
        rgb = [int(color[i:i+1], base=16) for i in range(0,6,2)]
        return rgb

if __name__ == '__main__':
    print('if selected')