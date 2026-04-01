let level = 1;
let currentClicks = 0;
let targetClicks = 0;
let baseTime = 7.0;
let timeLeft = 7.0;
let timerInterval;
let gameActive = false;
let nickname = "";

const stage = document.getElementById('stage');
const nicknameInput = document.getElementById('nickname-input');
const mainButton = document.getElementById('main-button');
const instructionText = document.getElementById('instruction');

// REKORU GÖSTER
document.getElementById('best-level').innerText = localStorage.getItem('reflex7_best') || 0;

document.querySelectorAll('.mode-btn').forEach(btn => {
    btn.addEventListener('click', (e) => {
        const nick = nicknameInput.value.trim();
        nickname = nick === "" ? "OYUNCU-Z" : nick.toUpperCase();
        baseTime = parseFloat(e.target.dataset.time);
        stage.classList.add('is-playing');
        setTimeout(() => { startGame(); }, 800);
    });
});

function startGame() {
    level = 1; gameActive = true; nextLevel();
}

function nextLevel() {
    clearInterval(timerInterval);
    currentClicks = 0;
    // Her 5 levelda bir hız %5 artar
    const speed = Math.max(0.4, 1 - (Math.floor(level/5) * 0.05));
    timeLeft = baseTime * speed;
    document.getElementById('level-count').innerText = level;
    generateMegaTask(); // Yeni nesil görev üretici
    startTimer();
}

function generateMegaTask() {
    const r = Math.random();
    mainButton.style.backgroundColor = "#4CAF50"; // Varsayılan Yeşil
    mainButton.style.color = "white";
    
    // 1. RENK TUZAĞI (Stroop Etkisi)
    if (r < 0.20) {
        const colors = [
            { name: "KIRMIZI", code: "#f44336", count: 7 },
            { name: "MAVİ", code: "#2196F3", count: 4 },
            { name: "SARI", code: "#ffeb3b", count: 4 }
        ];
        const pick = colors[Math.floor(Math.random() * colors.length)];
        mainButton.style.backgroundColor = pick.code;
        if (pick.name === "KIRMIZI") {
            targetClicks = 0;
            instructionText.innerHTML = "RENK KIRMIZI!<br>SAKIN BASMA!";
        } else {
            targetClicks = 1;
            instructionText.innerHTML = "RENK MAVİ/SARI İSE<br>1 KEZ DOKUN";
        }
    }
    // 2. MATEMATİK VE NEGATİF OYUNU
    else if (r < 0.40) {
        let n1 = Math.floor(Math.random() * 12) + 2;
        let n2 = Math.floor(Math.random() * 10);
        let res = n1 - n2;
        if (res <= 0) {
            targetClicks = 2;
            instructionText.innerHTML = `${n1} - ${n2} ≤ 0 ise<br>2 KEZ BAS!`;
        } else {
            targetClicks = res;
            instructionText.innerHTML = `${n1} - ${n2}<br>kere bas!`;
        }
    }
    // 3. SEVİYE BAZLI (ÇİFT/TEK) - YENİ!
    else if (r < 0.60) {
        if (level % 2 === 0) {
            targetClicks = 2;
            instructionText.innerHTML = `LEVEL ÇİFT!<br>2 KEZ BAS`;
        } else {
            targetClicks = 1;
            instructionText.innerHTML = `LEVEL TEK!<br>1 KEZ BAS`;
        }
    }
    // 4. KELİME VE HARF ANALİZİ
    else if (r < 0.80) {
        const words = ["ODAK", "HIZ", "DİKKAT", "REFLEX", "ZAMAN"];
        const word = words[Math.floor(Math.random()*words.length)];
        targetClicks = word.length;
        instructionText.innerHTML = `'${word}' kelimesindeki<br>harf sayısı kadar bas`;
    }
    // 5. GÖRSEL TUZAK (Emoji)
    else {
        const icons = [
            { img: "💣", task: "BOMBA! BASMA!", t: 0 },
            { img: "⚡", task: "YILDIRIM! 3 BAS!", t: 3 },
            { img: "🎯", task: "HEDEF! 1 BAS!", t: 1 }
        ];
        const pick = icons[Math.floor(Math.random() * icons.length)];
        targetClicks = pick.t;
        instructionText.innerHTML = `${pick.img}<br>${pick.task}`;
        if (pick.t === 0) mainButton.style.backgroundColor = "#333";
    }
}

function startTimer() {
    const bar = document.getElementById('timer-bar');
    const base = timeLeft;
    timerInterval = setInterval(() => {
        timeLeft -= 0.05;
        bar.style.strokeDashoffset = 301.6 - (timeLeft / base) * 301.6;
        if (timeLeft <= 0) {
            clearInterval(timerInterval);
            if (targetClicks === 0) { level++; nextLevel(); }
            else { gameOver("Süre Bitti!"); }
        }
    }, 50);
}

mainButton.addEventListener('click', () => {
    if (!gameActive) return;
    currentClicks++;
    
    if (targetClicks === 0 || (currentClicks > targetClicks)) {
        gameOver(targetClicks === 0 ? "Basmaman gerekiyordu!" : "Fazla bastın!");
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
    const best = localStorage.getItem('reflex7_best') || 0;
    if (level > best) localStorage.setItem('reflex7_best', level);
    
    document.getElementById('fail-message').innerText = msg;
    document.getElementById('final-level').innerText = level;
    document.getElementById('game-over-screen').classList.add('active');
    
    document.getElementById('final-score-details').innerText = `${nickname}: LEVEL ${level}`;
    document.getElementById('leaderboard-status-modal').classList.add('active');
}

document.getElementById('retry-button').onclick = () => location.reload();
document.getElementById('close-modal-button').onclick = () => document.getElementById('leaderboard-status-modal').classList.remove('active');