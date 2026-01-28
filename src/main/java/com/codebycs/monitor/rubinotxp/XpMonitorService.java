package com.codebycs.monitor.rubinotxp;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class XpMonitorService {

    private Map<String, Long> cacheHighscore = new HashMap<>();
    private final String WEBHOOK_URL = "https://discord.com/api/webhooks/1464595082287255617/iY6JxlWu0A0YzYoM7v2C7tXYnHImbANB9pdgyu423EnuXt3kN6VHYTCQ7nUCM4WHlbGb";
    private final long MINIMO_XP_ALERTA = 700000;

    @Scheduled(fixedRate = 180000)
    public void executarBot() {
        System.out.println("\n📈 [Daily Raw] A verificar...");
        String usuario = System.getProperty("user.name");
        WebDriver driver = null;

        // 1. Criamos uma lista para guardar os rushs encontrados nesta varredura
        List<String> listaDeRushs = new ArrayList<>();

        try {
            ChromeOptions options = new ChromeOptions();
            options.addArguments("user-data-dir=C:/Users/" + usuario + "/AppData/Local/Google/Chrome/PerfilBotXP");
            options.addArguments("--headless=new");
            options.addArguments("--log-level=3");
            driver = new ChromeDriver(options);

            for (int p = 1; p <= 3; p++) {
                driver.get("https://rubinot.net/?subtopic=highscores&world=Cellenium&category=16&currentpage=" + p);
                Thread.sleep(3000);

                List<WebElement> linhas = driver.findElements(By.cssSelector("tr"));

                // 2. Passamos a lista para o processador preencher
                processarPagina(linhas, listaDeRushs, p);
            }

            // 3. Após fechar as 3 páginas, se houver algo na lista, enviamos uma única mensagem
            if (!listaDeRushs.isEmpty()) {
                enviarRelatorioAgrupado(listaDeRushs);
            }

        } catch (Exception e) {
            System.err.println("❌ Erro na Raw: " + e.getMessage());
        } finally {
            if (driver != null) driver.quit();
        }
    }
    private void processarPagina(List<WebElement> linhas, List<String> listaDeRushs, int paginaAtual) {
        int rankBase = (paginaAtual - 1) * 25;
        int contadorLinha = 0;

        for (WebElement linha : linhas) {
            try {
                List<WebElement> colunas = linha.findElements(By.tagName("td"));
                if (colunas.size() >= 6) {
                    String nome = colunas.get(1).getText().trim();
                    String vocacao = colunas.get(2).getText().trim();
                    String level = colunas.get(4).getText().replaceAll("[^0-9]", "");
                    String pontosTextoRaw = colunas.get(5).getText().replaceAll("[^0-9]", "");

                    if (nome.isEmpty() || pontosTextoRaw.isEmpty() || nome.equalsIgnoreCase("Name")) continue;

                    contadorLinha++;
                    long pontosAtuais = Long.parseLong(pontosTextoRaw);
                    //long levelAtual = Long.parseLong(levelTexto);
                    int rankReal = rankBase + contadorLinha;

                    if (cacheHighscore.containsKey(nome)) {
                        long pontosAntigos = cacheHighscore.get(nome);
                        long ganho = pontosAtuais - pontosAntigos;

                        if (ganho > MINIMO_XP_ALERTA) {
                            // 4. Em vez de enviar agora, montamos a linha e guardamos na lista
                            String ganhoFmt = String.format("%,d", ganho).replace(',', '.');
                            String totalFmt = String.format("%,d", pontosAtuais).replace(',', '.');
                            //String linhaRush = "👤 `" + nome + "` (Lvl: " + levelAtual + ") 📈 **+" + ganhoFmt + "**";

                            // ⭐ Rank - Nome - Vocação - Level - Total (Ganho)
                            String linhaFormatada = String.format("⭐ #%d ----- **%s** ----- %s ----- Lvl: %s ----- %s (Subiu +%s Desde a ultima veirifcação)",
                                    rankReal, nome, vocacao, level, totalFmt, ganhoFmt);
                            listaDeRushs.add(linhaFormatada);

                            cacheHighscore.put(nome, pontosAtuais);
                        }
                    } else {
                        cacheHighscore.put(nome, pontosAtuais);
                    }
                }
            } catch (StaleElementReferenceException e) {
                continue; }
        }
    }

    private void enviarRelatorioAgrupado(List<String> rushs) {
        if (rushs == null || rushs.isEmpty()) return;

        String urlWebhook = WEBHOOK_URL;
        RestTemplate restTemplate = new RestTemplate();

        // Título inicial
        StringBuilder mensagem = new StringBuilder();
        mensagem.append("🚀 **RELATÓRIO DAILY RAW - RUBINOT**\n");
        mensagem.append("━━━━━━━━━━━━━━━━━━━━━━━━\n\n");

        for (String r : rushs) {
            // O limite do Discord é 2000. Usamos 1800 para ter margem de segurança.
            if (mensagem.length() + r.length() > 1800) {
                enviarParaDiscord(restTemplate, urlWebhook, mensagem.toString());

                // Reinicia o buffer para a próxima parte do relatório
                mensagem.setLength(0);
                mensagem.append("🔹 **Continuação do Relatório...**\n");
            }
            mensagem.append(r).append("\n\n");
        }

        // Envia o restante (o que sobrou no StringBuilder)
        if (mensagem.length() > 0) {
            mensagem.append("━━━━━━━━━━━━━━━━━━━━━━━━");
            enviarParaDiscord(restTemplate, urlWebhook, mensagem.toString());
        }
    }

    // Método auxiliar para fazer o POST de fato
    private void enviarParaDiscord(RestTemplate restTemplate, String url, String conteudo) {
        try {
            Map<String, String> corpo = new HashMap<>();
            corpo.put("content", conteudo);
            restTemplate.postForEntity(url, corpo, String.class);
            System.out.println("✅ Parte do relatório enviada com sucesso.");
        } catch (Exception e) {
            System.err.println("❌ Erro ao disparar Webhook: " + e.getMessage());
        }
    }



}
