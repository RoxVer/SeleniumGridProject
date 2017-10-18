package selenium.grid.app;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerOptions;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public abstract class BaseTest {
    protected WebDriver driver;
    protected WebDriverWait wait;
    String device = "Nexus 5X";

    private WebDriver getDriver(String browser) {
        DesiredCapabilities capabilities;
        if (browser.equals("firefox")) {
            System.setProperty(
                    "webdriver.gecko.driver",
                    new File(BaseTest.class.getResource("/geckodriver.exe").getFile()).getPath());
            return new FirefoxDriver();
        } else if (browser.equals("ie")) {
            System.setProperty(
                    "webdriver.ie.driver",
                    new File(BaseTest.class.getResource("/IEDriverServer.exe").getFile()).getPath());
            capabilities = DesiredCapabilities.internetExplorer();
            capabilities.setCapability(InternetExplorerDriver.NATIVE_EVENTS, false);            // disable native events to speed up typing
            capabilities.setCapability(InternetExplorerDriver.IE_ENSURE_CLEAN_SESSION, true);   // force clean session
            return new InternetExplorerDriver(new InternetExplorerOptions(capabilities));
        } else if (browser.equals("phantomjs")) {
            System.setProperty(
                    "phantomjs.binary.path",
                    new File(BaseTest.class.getResource("/phantomjs.exe").getFile()).getPath());
            return new PhantomJSDriver();
        } else if (browser.equals("android")) {
            System.setProperty(
                    "webdriver.chrome.driver",
                    new File(BaseTest.class.getResource("/chromedriver.exe").getFile()).getPath());

            Map<String, String> mobileEmulation = new HashMap<String, String>();
            mobileEmulation.put("deviceName", device);
            Map<String, Object> chromeOptions = new HashMap<String, Object>();
            chromeOptions.put("mobileEmulation", mobileEmulation);
            capabilities = DesiredCapabilities.chrome();
            capabilities.setCapability(ChromeOptions.CAPABILITY, chromeOptions);
            return new ChromeDriver(capabilities);
        } else {
            System.setProperty(
                    "webdriver.chrome.driver",
                    new File(BaseTest.class.getResource("/chromedriver.exe").getFile()).getPath());
            return new ChromeDriver();
        }
    }

    private RemoteWebDriver getRemoteDriver(String hubUrl, String browser) throws MalformedURLException {
        DesiredCapabilities capabilities;
        if (browser.equals("firefox")) {
            capabilities = DesiredCapabilities.firefox();
        } else if (browser.equals("ie")) {
            capabilities = DesiredCapabilities.internetExplorer();
        } else if (browser.equals("phantomjs")) {
            capabilities = DesiredCapabilities.phantomjs();
        } else if (browser.equals("android")) {
            Map<String, String> mobileEmulation = new HashMap<String, String>();
            mobileEmulation.put("deviceName", device);
            Map<String, Object> chromeOptions = new HashMap<String, Object>();
            chromeOptions.put("mobileEmulation", mobileEmulation);
            capabilities = DesiredCapabilities.chrome();
            capabilities.setCapability(ChromeOptions.CAPABILITY, chromeOptions);
        } else {
            capabilities = DesiredCapabilities.chrome();
        }
        return new RemoteWebDriver(new URL(hubUrl), capabilities);
    }

    @BeforeClass
    @Parameters({"selenium.hub", "selenium.browser"})
    public void setUp(@Optional("http://localhost:4444/wd/hub") String hubURL, @Optional("android") String browser) throws MalformedURLException {
        //hubURL = http://localhost:4444/wd/hub
        driver = hubURL.isEmpty() ? getDriver(browser) : getRemoteDriver(hubURL, browser);
        driver.manage().timeouts().implicitlyWait(15, TimeUnit.SECONDS);
        driver.manage().timeouts().pageLoadTimeout(30, TimeUnit.SECONDS);

        wait = new WebDriverWait(driver, 5);
    }

    @AfterClass
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
