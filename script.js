let level = 1;
let currentClicks = 0;
let targetClicks = 0;
let baseTime = 7.0;
let timeLeft = 7.0;
let timerInterval;
let gameActive = false;
let startTime;
let pendingTask = null;

const mainButton = document.getElementById('main-button');
const instructionText = document.getElementById('instruction');
const levelCount = document.getElementById('level-count');
const timerBar = document.getElementById('timer-bar');
const stage = document.getElementById('stage');
const modeSelection = document.getElementById('mode-selection');

// REKORU YÜKLE
document.getElementById('best-level').innerText = localStorage.getItem('reflex7_best') || 0;

// MOD SEÇİMİ VE BAŞLATMA
document.querySelectorAll('.mode-btn').forEach(btn => {
    btn.addEventListener('click', (e) => {
        baseTime = parseFloat(e.target.dataset.time);
        modeSelection.classList.add('hide');
        stage.classList.add('centered');
        setTimeout(startGame, 800);
    });
});

function startGame() {
    level = 1; 
    gameActive = true; 
    startTime = Date.now(); 
    nextLevel();
}

function nextLevel() {
    clearInterval(timerInterval);
    currentClicks = 0;
    
    // Her 5 seviyede bir oyun hızlanır (Dinamik Hızlanma)
    const speedFactor = Math.max(0.5, 1 - (Math.floor(level/5) * 0.05));
    timeLeft = baseTime * speedFactor;
    
    levelCount.innerText = level;

    if (pendingTask) {
        setTask(pendingTask);
        pendingTask = null;
    } else {
        generateMegaTask();
    }
    startTimer();
}

function setTask(task) {
    instructionText.innerHTML = task.trapHtml || task.text;
    targetClicks = task.target;
    mainButton.style.backgroundColor = task.color || "#4CAF50";
}

function generateMegaTask() {
    const r = Math.random();
    
    // 1. MATEMATİK VE NEGATİF TUZAĞI
    if (r < 0.30) {
        let n1 = Math.floor(Math.random() * 10) + 1;
        let n2 = Math.floor(Math.random() * 15) - 5;
        let n3 = Math.floor(Math.random() * 5);
        
        let result = n1 - n2 + n3;
        let formula = `${n1} - (${n2}) + ${n3}`;

        if (result <= 0) {
            // Sonuç negatif veya 0 ise kural: 2 kez bas
            setTask({ text: `${formula}<br>Sonuç ≤ 0 ise 2 kez bas!`, target: 2, color: "#9c27b0" });
        } else {
            setTask({ text: `${formula}<br>kere bas!`, target: result });
        }
    } 
    // 2. HAFIZA
    else if (r < 0.45) {
        const nextVal = Math.floor(Math.random() * 3) + 2;
        pendingTask = { text: `Şimdi ${nextVal} kere bas!`, target: nextVal };
        setTask({ text: "SABRET!<br>Bir sonrakini bekle.", target: 0, color: "#ff9800" });
    }
    // 3. RENK / MANTIK
    else if (r < 0.60) {
        const trapType = Math.random() > 0.5;
        if (trapType) {
            setTask({ text: "Buton KIRMIZI ise<br>ASLA basma", target: 0, color: "#f44336" });
        } else {
            setTask({ text: "Yazı beyazsa<br>1 kez dokun", target: 1, color: "#2196F3" });
        }
    }
    // 4. KELİME ANALİZİ
    else if (r < 0.75) {
        const words = ["REFLEX", "ZAMAN", "BUTON", "DİKKAT", "HIZLI", "ODAK"];
        const word = words[Math.floor(Math.random()*words.length)];
        setTask({ text: `'${word}' kelimesindeki<br>harf sayısı kadar bas`, target: word.length });
    }
    // 5. GÖRSEL TUZAKLAR
    else if (r < 0.90) {
        const visuals = [
            { h: "🐱<br>Kedi ise basma!", t: 0 },
            { h: "🦁<br>Aslan ise 3 bas!", t: 3 },
            { h: "🍎<br>Elma ise 1 bas!", t: 1 }
        ];
        const v = visuals[Math.floor(Math.random()*visuals.length)];
        setTask({ trapHtml: v.h, target: v.t });
    }
    else {
        setTask({ text: "Acele et!<br>4 kere bas!", target: 4 });
    }
}

function startTimer() {
    const totalDash = 301.6;
    const currentBase = timeLeft; // O anki level süresi
    
    timerInterval = setInterval(() => {
        timeLeft -= 0.05;
        const offset = totalDash - (timeLeft / currentBase) * totalDash;
        timerBar.style.strokeDashoffset = offset;

        if (timeLeft <= 0) {
            clearInterval(timerInterval);
            if (targetClicks === 0) { level++; nextLevel(); }
            else { gameOver("Süre Bitti!<br>Çok yavaşsın."); }
        }
    }, 50);
}

mainButton.addEventListener('click', () => {
    if (!gameActive) return;
    
    currentClicks++;
    
    // YANLIŞ BASIŞTA EKRANI SALLA
    if (targetClicks === 0 || (currentClicks > targetClicks)) {
        document.body.classList.add('shake');
        setTimeout(() => document.body.classList.remove('shake'), 400);
        
        let msg = targetClicks === 0 ? "Basma demiştim!" : "Fazla bastın!";
        gameOver(msg);
        return;
    }
    
    if (currentClicks === targetClicks) {
        level++;
        nextLevel();
    }
});

function gameOver(msg) {
    gameActive = false;
    clearInterval(timerInterval);
    
    // REKOR KONTROLÜ
    const best = localStorage.getItem('reflex7_best') || 0;
    if (level > best) {
        localStorage.setItem('reflex7_best', level);
    }

    const totalTime = Math.floor((Date.now() - startTime) / 1000);
    document.getElementById('fail-message').innerHTML = msg;
    document.getElementById('final-level').innerText = level;
    document.getElementById('total-time').innerText = totalTime;
    document.getElementById('game-over-screen').classList.add('active');
}

document.getElementById('retry-button').onclick = () => { location.reload(); };