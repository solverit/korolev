package gp

import java.io.{File, PrintWriter}
import java.util.regex.Pattern

import org.openqa.selenium.remote.{LocalFileDetector, RemoteWebDriver}
import org.openqa.selenium.support.ui.{ExpectedConditions, WebDriverWait}
import org.openqa.selenium.{By, Platform, WebDriver}
import tools._

import scala.collection.JavaConverters._
import scala.concurrent.duration._

object GuineaPigScenarios {

  private val appUrl = "http://localhost:8000"

  private def wait(wd: WebDriver) = new WebDriverWait(wd, 60)

  val allInOne = scenario("All-in-one scenario")(
    step("Page should be open successfully") { wd =>
      // Open browser
      wd.get(appUrl + "/")
      assert("Title", wd.getTitle == "The Test App")
      assert("Connected",
        wait(wd).until(
          ExpectedConditions.textMatches(
            By.id("debug-log-label"),
            Pattern.compile("connected")
          )
        )
      ) // Wait for JS initialization
    },
    step("Switch to second tab") { wd =>
      wd.findElement(By.id("tab2")).click()
      assert("Url should be tab2", wait(wd).until(ExpectedConditions.urlToBe(s"$appUrl/tab2")))
    },
    step("Click on first ToDo") { wd =>
      val firstToDoCheckBox = wd
        .findElements(By.className("todo_checkbox"))
        .asScala
        .head
      firstToDoCheckBox.click()
      assert("Todo should be checked", wait(wd).until(
        ExpectedConditions.attributeContains(
          firstToDoCheckBox,
          "class",
          "todo_checkbox__checked"
        )
      ))
    },
    step("Todo should be added after 'Add todo' click") { implicit wd =>
      // Add new todo
      val newTodoText = "Hello world"
      val input = wd.findElement(By.id("todo-input"))
      input.scrollTo()
      input.sendKeys(newTodoText)
      wd.findElement(By.id("todo-submit-button")).click()
      // Check new dod
      assert(s"Last todo text should contain $newTodoText", wait(wd).until(
        ExpectedConditions.textToBe(
          By.xpath("(//div[@class='todo'])[last()]"),
          newTodoText
        )
      ))
    },
    step("Field should be empty after todo was added") { wd =>
      val value = wd.findElement(By.id("todo-input")).getAttribute("value")
      assert("Field should be empty", value == "")
    },
    step("Uploaded text file should be displayed") { wd =>
      val shouldRun = wd match {
        case r: RemoteWebDriver =>
          r.setFileDetector(new LocalFileDetector())
          r.getCapabilities.getPlatform match {
            // The SafariDriver does not support file uploads
            // https://github.com/seleniumhq/selenium-google-code-issue-archive/issues/4220
            // LocalFileDetector does not work on Android
            case Platform.MAC | Platform.ANDROID => false
            // TODO understand reason why test fails on Edge
            case _ if r.getCapabilities.getBrowserName == "MicrosoftEdge" => false
            case _ => true
          }
        case _ => true
      }
      if (shouldRun) {
        val text = "I'm cow"
        val file = File.createTempFile("korolev-upload-test", "cow")
        new PrintWriter(file) {
          write(text)
          close()
        }
        wd.findElement(By.name("upload-input")).sendKeys(file.getAbsolutePath)
        wd.findElement(By.id("upload-button")).click()
        assert(s"upload-text.textContent should be $text", wait(wd).until(
          ExpectedConditions.textToBe(
            By.id("upload-text"),
            text
          )
        ))
      }
      else {
        StepResult.CowardlySkipped("Not supported")
      }
    },
    step("Delay should be performed") { wd =>
      val el = wd.findElement(By.id("delay-text"))
      el.click()
      sleep(200.millis)
      assert(
        "delay-text should be 'Wait a second'",
        el.getText == "Wait a second"
      )
      sleep(900.millis)
      assert(
        "delay-text should be 'Click me'",
        el.getText == "Click me"
      )
    }
  )

  val all = Seq(allInOne)
}