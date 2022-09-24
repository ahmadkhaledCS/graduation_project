from CustomObjects import MyDriver
from fastapi import FastAPI

app = FastAPI()


@app.get("/")
async def root():
    return "bau api"


@app.get("/profile/{studentId}/{password}")
async def profile(studentId: str, password: str):
    # initialize the web browser
    driver = MyDriver()

    if not driver.getMainSiteAndLogin(studentId, password):
        return {"wrong": "user or pass"}
    else:
        return {'profile': driver.getProfile()}


@app.get("/allowed/{studentId}/{password}")
async def allowed(studentId: str, password: str):
    # initialize the web browser
    driver = MyDriver()

    if not driver.getMainSiteAndLogin(studentId, password):
        return {"wrong": "user or pass"}
    else:
        return {'courses': driver.getAllowedCourses()}


@app.get("/plan/{studentId}/{password}")
async def plan(studentId: str, password: str):
    # initialize the web browser
    driver = MyDriver()

    if not driver.getMainSiteAndLogin(studentId, password):
        return {"wrong": "user or pass"}
    else:
        return {'plan': driver.getPlan()}
