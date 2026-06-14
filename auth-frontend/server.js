const express = require('express');
const axios = require('axios');
const path = require('path');
const app = express();

// Configurações para o Node entender os dados do formulário e JSON
app.use(express.urlencoded({ extended: true }));
app.use(express.json());
app.use(express.static('public')); 

// 1. GET / serve index.html 
app.get('/', (req, res) => {
    res.sendFile(path.join(__dirname, 'public', 'index.html'));
});

// 2. POST /send-code 
app.post('/send-code', async (req, res) => {
    const email = req.body.email; // Pega o e-mail do formulário
    try {
        // chama POST http://localhost:8081/auth/request-code [cite: 40]
        await axios.post('http://localhost:8081/auth/request-code', { email: email });
        
        // redireciona para /verify?email=... [cite: 41]
        res.redirect(`/verify?email=${encodeURIComponent(email)}`);
    } catch (error) {
        console.error("Erro ao solicitar código:", error.message);
        res.status(500).send("Erro ao processar a solicitação.");
    }
});

// 3. GET /verify serve verify.html [cite: 42, 43]
app.get('/verify', (req, res) => {
    res.sendFile(path.join(__dirname, 'public', 'verify.html'));
});

// 4. POST /verify-code 
app.post('/verify-code', async (req, res) => {
    const { email, codigo } = req.body;
    try {
        // chama POST http://localhost:8081/auth/verify-code [cite: 45]
        const response = await axios.post('http://localhost:8081/auth/verify-code', { email: email, code: codigo });
        
        // Devolve o Token JWT para o frontend
        res.json(response.data);
    } catch (error) {
        console.error("Erro na validação:", error.message);
        res.status(401).json({ erro: "Código inválido ou expirado." }); // Caso contrário, exibe erro 
    }
});

app.listen(3000, () => {
    console.log('Frontend rodando 100% no padrão do PDF na porta 3000!');
});