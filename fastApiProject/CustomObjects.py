from selenium.webdriver.common.by import By
from selenium import webdriver
from selenium.webdriver.chrome.options import Options
import json
import re


class MyDriver:
    def __init__(self):
        # initializing the Chrome browser
        options = Options()

        # open in headless state in server
        '''
        options.headless = True
        options.add_argument(f'user-agent="Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, '
                             f'like Gecko) Chrome/84.0.4147.56 Safari/537.36"') 
        '''
        # options.add_experimental_option("prefs", {'profile.managed_default_content_settings.images': 2})
        self.driver = webdriver.Chrome("chromedriver.exe", options=options)

    def getMainSiteAndLogin(self, studentId, password):
        # open main site
        self.driver.get(f'file://C:/Users/Akmad/PycharmProjects/fastApiProject/main.html')
        # enter username and password in fields
        self.driver.find_element(By.NAME, "username").send_keys(studentId)
        self.driver.find_element(By.NAME, "password").send_keys(password)
        # click on the submit button to log in
        self.driver.find_element(By.CSS_SELECTOR, "input[type=submit]").click()
        self.driver.execute_script('window.stop();')
        # if we can find the login failed text then close the browser and return false
        try:
            if self.driver.find_element(By.XPATH, '//*[@id="workspace"]/table[1]/tbody/tr/td[1]/div/h3'):
                self.driver.close()
                return False
        # else return true because we successfully logged in
        except:
            return True

    def execute(self, script):
        # wipes the website with javascript document.open function
        self.driver.execute_script('document.open()')
        # gets the student plan as a json object
        self.driver.execute_script(script)
        # loading the text from browser to a python dictionary
        result = json.loads(self.driver.find_element(By.XPATH, "/html/body").text)
        return result

    def getPlan(self):
        return self.execute('document.writeln(JSON.stringify(getStudentClassifications()));')

    def getProfile(self):
        result = self.execute('document.writeln(JSON.stringify(getStudent()));')
        # adding the email and password from the announcements
        result['email'], result['emailPassword'] = self.getEmailAndPass()
        return result

    def getAllowedCourses(self):
        # script to add times to the courses and return them
        script = 'var courses=getAllowedCourses();' \
                 'for(var i=0;i<courses.length;i++)' \
                    'courses[i]["available classrooms"]=getCourseSections(i+1);' \
                 'document.write(JSON.stringify(courses));'
        return self.execute(script)

    def getCurrentSemester(self):
        return self.execute('document.write(JSON.stringify(getRegisteredCourses()));')

    def getEmailAndPass(self):
        # a javascript script to get the email and password announcement
        script = 'var a=getAnnouncements();' \
                 'for(var i=0;i<a.length;i++){' \
                 'if(a[i].text.includes("http://mail.office365.com")){' \
                 'document.writeln(a[i].text);' \
                 'break;' \
                 '}' \
                 '}'
        # wipes the website with javascript document.open function
        self.driver.execute_script('document.open()')
        # execute the script above
        self.driver.execute_script(script)
        # we search for the email and password with regex inside the announcement
        return re.findall('bau@\\d{4}|\\d+@std.bau.edu.jo', self.driver.page_source)
