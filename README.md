# 🚀 Rubinot XP Monitor

Um bot automatizado desenvolvido em **Java** e **Spring Boot** para monitorar o progresso de experiência (XP) dos jogadores no servidor Rubinot (Tibia).

## 🛠️ Tecnologias Utilizadas
- **Java 21** & **Spring Boot**
- **Selenium WebDriver**: Automação para coleta de dados (Web Scraping).
- **Discord Webhooks**: Notificações em tempo real.
- **Maven**: Gerenciamento de dependências.

## 🌟 Diferenciais Técnicos
- **Tratamento de Erros Resiliente**: Implementação de captura específica para `StaleElementReferenceException`, garantindo que o bot não interrompa a varredura caso o site atualize o DOM de forma assíncrona.
- **Otimização de Notificações**: Sistema de relatório agrupado para evitar o *Rate Limit* da API do Discord, enviando uma lista formatada em vez de múltiplas mensagens.
- **Persistência em Cache**: Uso de `HashMap` para rastrear ganhos de XP entre as varreduras.

## 📋 Como funciona
O bot varre as 3 primeiras páginas de Highscore a cada 2 minutos. Se um jogador ganhar mais de 70.000 de XP, ele é adicionado a um relatório formatado e enviado ao Discord.