package mx.unadm.rupe.selenium;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import static org.junit.jupiter.api.Assertions.*;

@Disabled("Activar cuando ChromeDriver esté instalado y RUPE esté ejecutándose.")
class RupeSeleniumTest {
    private final String baseUrl = System.getProperty("rupe.url", "http://localhost:8080");

    @Test
    void pcb009_registroCompletoDesdeNavegador() {
        WebDriver driver = new ChromeDriver();
        try {
            driver.get(baseUrl + "/reportes/nuevo");
            assertTrue(driver.getPageSource().contains("Registrar perro"));
        } finally { driver.quit(); }
    }

    @Test
    void pcb010_colorNoAceptaNumeros() {
        WebDriver driver = new ChromeDriver();
        try {
            driver.get(baseUrl + "/reportes/nuevo");
            WebElement color = driver.findElement(By.name("color"));
            color.sendKeys("Negro123");
            assertFalse(color.getAttribute("validationMessage").isBlank());
        } finally { driver.quit(); }
    }

    @Test
    void pcb011_senasNoAceptanSignos() {
        WebDriver driver = new ChromeDriver();
        try {
            driver.get(baseUrl + "/reportes/nuevo");
            WebElement senas = driver.findElement(By.name("senas"));
            senas.sendKeys("Collar rojo!!!");
            assertFalse(senas.getAttribute("validationMessage").isBlank());
        } finally { driver.quit(); }
    }

    @Test
    void pcb012_fechaEsObligatoria() {
        WebDriver driver = new ChromeDriver();
        try {
            driver.get(baseUrl + "/reportes/nuevo");
            WebElement fecha = driver.findElement(By.name("fechaExtravio"));
            assertEquals("true", fecha.getAttribute("required"));
        } finally { driver.quit(); }
    }

    @Test
    void pcb013_consultaPorFolioVisible() {
        WebDriver driver = new ChromeDriver();
        try {
            driver.get(baseUrl + "/reportes/consulta");
            assertTrue(driver.getPageSource().contains("Consultar"));
        } finally { driver.quit(); }
    }

    @Test
    void pcb014_loginConCaptchaVisible() {
        WebDriver driver = new ChromeDriver();
        try {
            driver.get(baseUrl + "/admin/login");
            assertTrue(driver.getPageSource().contains("CAPTCHA"));
        } finally { driver.quit(); }
    }

    @Test
    void pcb015_panelNoAbreSinSesion() {
        WebDriver driver = new ChromeDriver();
        try {
            driver.get(baseUrl + "/admin/dashboard");
            assertTrue(driver.getCurrentUrl().contains("/admin/login"));
        } finally { driver.quit(); }
    }

    @Test
    void pcb016_pistaFormularioVisible() {
        WebDriver driver = new ChromeDriver();
        try {
            driver.get(baseUrl + "/pistas/nueva");
            assertTrue(driver.getPageSource().contains("pista") || driver.getPageSource().contains("Pista"));
        } finally { driver.quit(); }
    }
}
