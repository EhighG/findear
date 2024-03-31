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
# Create your models here.

logger = logging.getLogger(__name__)
class matchModel():
    
    def __init__(self) -> None:
        load_dotenv()
        path = os.getenv("MODEL_PATH")
        #model = fasttext.load_model(path)
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

    
    def loadFastText(self):
        pass
    
    def loadColor(self):
        pass

    def test(self):
        print('teststest')
        return 'test'


if __name__ == '__main__':
    print('if selected')