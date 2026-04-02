/* Reflex7 v0.9 - "The Architect" Full Script
    Özellikler: Paket Seviye Sistemi, Global Kurallar, Dinamik Hedefleme
*/

let level = 1;
let currentClicks = 0;
let targetClicks = 0;
let baseTime = 7.0;
let timeLeft = 7.0;
let timerInterval;
let gameActive = false;
let startTime;
let nickname = "";

// PAKET VE DURUM YÖNETİMİ
let packageQueue = []; // Sıradaki özel görevlerin kuyruğu
let lastTarget = 0;    // Bir önceki adımda basılan sayı
let globalRule = null; // Aktif olan özel kurallar (Mavi/Fare kuralı)

// DOM ELEMENTLERİ
const stage = document.getElementById('stage');
const nicknameInput = document.getElementById('nickname-input');
const mainButton = document.getElementById('main-button');
const instructionText = document.getElementById('instruction');
const levelCount = document.getElementById('level-count');
const timerBar = document.getElementById('timer-bar');

// REKORU YÜKLE
document.getElementById('best-level').innerText = localStorage.getItem('reflex7_best') || 0;

// BAŞLATMA DÜĞMELERİ
document.querySelectorAll('.mode-btn').forEach(btn => {
    btn.addEventListener('click', (e) => {
        const nick = nicknameInput.value.trim();
        nickname = nick === "" ? "OYUNCU-Z" : nick.toUpperCase();
        baseTime = parseFloat(e.target.dataset.time);
        
        // ASİMETRİK KAYMA VE BAŞLAMA
        stage.classList.add('is-playing');
        setTimeout(() => {
            startGame();
        }, 800);
    });
});

function startGame() {
    level = 1; 
    gameActive = true; 
    startTime = Date.now(); 
    packageQueue = []; 
    globalRule = null; 
    nextLevel();
}

function nextLevel() {
    clearInterval(timerInterval);
    currentClicks = 0;
    
    // Hızlanma Eğrisi: Her 5 seviyede bir %5 hızlanır
    const speedFactor = Math.max(0.4, 1 - (Math.floor(level/5) * 0.05));
    timeLeft = baseTime * speedFactor;
    levelCount.innerText = level;

    // PAKET KONTROLÜ
    if (packageQueue.length > 0) {
        let nextTask = packageQueue.shift();
        executeTask(nextTask);
    } else {
        generateMegaTask();
    }
    startTimer();
}

// GÖREVİ EKRANA VE MANTIĞA YANSITAN ANA FONKSİYON
function executeTask(task) {
    // Dinamik matematik hesaplaması (Önceki adımdan fazla basma)
    if (task.type === "chain_math") {
        task.target = lastTarget + 1;
    }
    
    targetClicks = task.target;
    instructionText.innerHTML = task.text;
    
    // UI Güncellemeleri
    instructionText.style.color = task.fontColor || "white";
    mainButton.style.backgroundColor = task.btnColor || "#4CAF50";
    
    // Global kural set etme (Mavi yazı/Fare takibi)
    if (task.setRule) globalRule = task.setRule;
    
    lastTarget = targetClicks;
}

function generateMegaTask() {
    const r = Math.random();

    // %35 İHTİMALLE ÖZEL PAKET BAŞLAT
    if (r < 0.35) {
        startRandomPackage();
    } else {
        // STANDART GÖREVLER
        mainButton.style.backgroundColor = "#4CAF50";
        instructionText.style.color = "white";

        // Global kural check: Fare kuralı aktifse rastgele görevleri ez
        if (globalRule === "mouse_rule" && Math.random() < 0.2) {
            targetClicks = 3;
            instructionText.innerHTML = "🐭<br>Fare sayısı kadar bas"; // Görünüşte 1, kural gereği 3
        } else {
            // Normal matematik veya hız görevi
            targetClicks = Math.floor(Math.random() * 4) + 1;
            instructionText.innerHTML = `Hızlıca ${targetClicks} kez bas!`;
        }
        lastTarget = targetClicks;
    }
}

// SENİN TASARLADIĞIN ÖZEL PAKETLER
function startRandomPackage() {
    const p = Math.random();

    // 1. "GELECEKTEN GELEN TUZAK" (2'Lİ)
    if (p < 0.25) {
        packageQueue = [
            { text: "Sonraki adımda ASLA basma!<br>Şimdi 1 kez bas", target: 1 },
            { text: "2 KERE BAS", target: 0, btnColor: "#f44336" }
        ];
    }
    // 2. "ZİNCİRLEME MATEMATİK" (3'LÜ)
    else if (p < 0.50) {
        let start = Math.floor(Math.random() * 3) + 1;
        packageQueue = [
            { text: `Matematik: ${start+2} - 2`, target: start },
            { text: "Önceki adımdan 1 fazla bas", type: "chain_math" },
            { text: "Önceki adımdan 1 fazla bas", type: "chain_math" }
        ];
    }
    // 3. "MAVİ RENK PARADOKSU" (4'LÜ)
    else if (p < 0.75) {
        packageQueue = [
            { text: "Bundan sonra mavi renkli yazı görürsen 1 kez bas",btnColor: "#2196F3", target: 1, setRule: "blue_rule" },
            { text: "5 - 2", target: 3 },
            { text: "Mavi", target: 0, fontColor: "white", btnColor: "#2196F3" }, // Mavi buton/kelime ama beyaz font
            { text: "BASMA", target: 1, fontColor: "#2196F3" } // Mavi font kuralı her şeyi ezer
        ];
    }
    // 4. "FARE TAKİBİ" (5'Lİ)
    else {
        packageQueue = [
            { text: "Fare görürsen 3 kez bas! <br> Şimdi 1 Kez!", target: 1, setRule: "mouse_rule" },
            { text: "B harfi alfabede kaçıncı sırada?", target: 2 },
            { text: "3 + 1", target: 4 },
            { text: "BAS BAS BAS BAS BAS BAS", target: 6 },
            { text: "🐭🐭<br>Fare sayısı kadar bas!", target: 3 } // Görünüşte 2, kuralda 3
        ];
    }
    executeTask(packageQueue.shift());
}

function startTimer() {
    const totalDash = 301.6;
    const currentBase = timeLeft;
    timerInterval = setInterval(() => {
        timeLeft -= 0.05;
        const offset = totalDash - (timeLeft / currentBase) * totalDash;
        timerBar.style.strokeDashoffset = offset;

        if (timeLeft <= 0) {
            clearInterval(timerInterval);
            // Eğer hedef 0 ise (basma görevi) sürenin bitmesi başarıdır
            if (targetClicks === 0) { level++; nextLevel(); }
            else { gameOver("Süre Bitti!<br>Çok yavaşsın."); }
        }
    }, 50);
}

// TIKLAMA MANTIĞI VE HATA KONTROLÜ
mainButton.addEventListener('click', () => {
    if (!gameActive) return;
    currentClicks++;
    
    // Yanlış basma veya fazla basma kontrolü
    if (targetClicks === 0 || (currentClicks > targetClicks)) {
        // Hata sallantısı efekti tetiklenebilir
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
    if (level > best) {
        localStorage.setItem('reflex7_best', level);
    }

    const totalTime = Math.floor((Date.now() - startTime) / 1000);
    
    // UI Güncelleme
    document.getElementById('fail-message').innerHTML = msg;
    document.getElementById('final-level').innerText = level;
    document.getElementById('total-time').innerText = totalTime;
    document.getElementById('game-over-screen').classList.add('active');

    // SKORU KAYDET (GLOBAL MODAL)
    recordScore(level);
}

function recordScore(finalLevel) {
    const modal = document.getElementById('leaderboard-status-modal');
    const details = document.getElementById('final-score-details');
    details.innerHTML = `${nickname}: LEVEL ${finalLevel}`;
    modal.classList.add('active');
}

// MODAL VE RETRY BUTONLARI
document.getElementById('retry-button').onclick = () => { location.reload(); };
document.getElementById('close-modal-button').onclick = () => {
    document.getElementById('leaderboard-status-modal').classList.remove('active');
};