const express = require('express');
const axios = require('axios');
const path = require('path');
const app = express();

app.use(express.urlencoded({ extended: true }));
app.use(express.json());
app.use(express.static('public')); 

// 1. GET / serve index.html 
app.get('/', (req, res) => {
    res.sendFile(path.join(__dirname, 'public', 'index.html'));
});

// 2. POST /send-code 
app.post('/send-code', async (req, res) => {
    const email = req.body.email;
    try {
        await axios.post('http://localhost:8081/auth/request-code', { email: email });
        res.redirect(`/verify?email=${encodeURIComponent(email)}`);
    } catch (error) {
        console.error("Erro ao solicitar código:", error.message);
        res.status(500).send("Erro ao processar a solicitação.");
    }
});

// 3. GET /verify serve verify.html
app.get('/verify', (req, res) => {
    res.sendFile(path.join(__dirname, 'public', 'verify.html'));
});

// 4. POST /verify-code 
app.post('/verify-code', async (req, res) => {
    const { email, codigo } = req.body;
    try {
        const response = await axios.post('http://localhost:8081/auth/verify-code', { 
            email: email, 
            codigo: codigo 
        });
        res.json(response.data);
    } catch (error) {
        console.error("Erro na validação:", error.message);
        res.status(401).json({ erro: "Código inválido ou expirado." });
    }
});

// 5. GET /register
app.get('/register', (req, res) => {
    res.sendFile(path.join(__dirname, 'public', 'register.html'));
});

// 6. POST /register - atualiza perfil
app.post('/register', async (req, res) => {
    try {
        const token = req.headers.authorization;
        console.log("TOKEN:", token);
        console.log("BODY:", req.body);

        const response = await axios.post(
            'http://localhost:8081/users/update-profile',
            req.body,
            {
                headers: {
                    'Authorization': token
                }
            }
        );
        res.json(response.data);
    } catch (error) {
        console.log("ERRO STATUS:", error.response?.status);
        console.log("ERRO DATA:", error.response?.data);
        console.log("ERRO:", error.message);
        res.status(500).send('Erro ao atualizar perfil');
    }
});

// 7. GET /dashboard
app.get('/dashboard', (req, res) => {
    res.sendFile(path.join(__dirname, 'public', 'dashboard.html'));
});

// 8. Proxy para endpoint protegido
app.get('/api/protected', async (req, res) => {
    try {
        const token = req.headers.authorization;
        const response = await axios.get('http://localhost:8081/users/test/customer', {
            headers: { 'Authorization': token }
        });
        res.send(response.data); 
    } catch (error) {
        res.status(403).send('Acesso negado');
    }
});

// 9. Proxy para /users/me
app.get('/users/me', async (req, res) => {
    try {
        const token = req.headers.authorization;
        const response = await axios.get('http://localhost:8081/users/me', {
            headers: { 'Authorization': token }
        });
        res.json(response.data);
    } catch (error) {
        res.status(403).json({ erro: 'Acesso negado' });
    }
});

app.listen(3000, () => {
    console.log('Frontend rodando em http://localhost:3000');
});