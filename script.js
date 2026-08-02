/* Reflex7 v1.1.0 - Session and Polish Edition */

const STORAGE_KEYS = {
    legacyBestLevel: 'reflex7_best',
    language: 'reflex7_language',
    sound: 'reflex7_sound',
    tipSeen: 'reflex7_tip_seen',
    discoveries: 'reflex7_discoveries',
    bestLevel: (mode) => `reflex7_best_level_${mode}`,
    bestScore: (mode) => `reflex7_best_score_${mode}`
};
const NICKNAME_MAX_LENGTH = 12;
const TOTAL_TIMER_DASH = 301.6;

const translations = {
    tr: {
        'document.title': 'Reflex7 - Elite Edition v1.1.0',
        'menu.languageSelector': 'Dil seçimi',
        'menu.turkish': 'Türkçe',
        'menu.english': 'İngilizce',
        'menu.bestLevel': 'En Yüksek: Seviye {level}',
        'menu.nicknamePrompt': 'Yerel skorun için takma adını gir ve hızını seç:',
        'menu.nicknameLabel': 'TAKMA AD:',
        'menu.nicknamePlaceholder': 'ÖR: ALFA-Z',
        'menu.slowMode': 'YAVAŞ (7 sn)',
        'menu.fastMode': 'HIZLI (4 sn)',
        'menu.start': 'BAŞLA',
        'game.level': 'SEVİYE: {level}',
        'result.title': 'ELENDİN!',
        'result.level': 'Seviye: {level}',
        'result.time': 'Süre: {seconds} sn',
        'result.retry': 'Tekrar Dene',
        'localScore.title': 'YEREL SKORUN HAZIR!',
        'localScore.details': '{nickname}: SEVİYE {level}',
        'localScore.note': 'En yüksek seviyen yalnızca bu tarayıcıda saklanır. Hiçbir skor sunucuya gönderilmez.',
        'common.close': 'TAMAM',
        'player.defaultNickname': 'OYUNCU-Z',
        'task.quickPress': 'Hızlıca {count} kez bas!',
        'task.futureWarning': 'Sonraki adımda ASLA basma!\nŞimdi 1 kez bas.',
        'task.doNotPressTrap': '2 KERE BAS',
        'task.mathSubtract': 'Matematik: {value} - 2',
        'task.chainMore': 'Önceki adımdan 1 fazla bas.',
        'task.blueRule': 'Mavi görürsen 1 kez bas.',
        'task.fiveMinusTwo': '5 - 2',
        'task.blueWord': 'Mavi',
        'task.doNotPress': 'BASMA',
        'task.mouseRule': 'Fare görürsen 3 kez bas!\nŞimdi 1 kez bas.',
        'task.threePlusOne': '3 + 1',
        'task.pressThree': 'BAS BAS BAS',
        'task.mouseRuleAction': '🐭🐭\nFare kuralına göre bas!',
        'task.mouseOverride': '🐭\nFare kuralına göre bas!',
        'task.holdStart': 'BASILI TUT!\nYeşil olunca bırak!',
        'task.holdWait': 'BEKLE...',
        'task.holdRelease': 'BIRAK!',
        'task.colorWait': 'SADECE YEŞİL\nolunca bas!',
        'task.evadeCatch': 'YAKALA',
        'task.sequenceButton': 'Sıra düğmesi {number}',
        'task.evadeButton': 'Hareketli hedef, {remaining} basış kaldı',
        'failure.shouldNotPress': 'Basmaman gerekiyordu!',
        'failure.tooManyPresses': 'Fazla bastın!',
        'failure.timeoutSlow': 'Süre bitti!\nÇok yavaşsın.',
        'failure.earlyRelease': 'Erken bıraktın!',
        'failure.holdTimeout': 'Süre dolmadan bırakmadın!',
        'failure.inputCancelled': 'Basılı tutma kesildi!',
        'failure.wrongColor': 'Yanlış renkte bastın!',
        'failure.colorTimeout': 'Zamanında basmadın!',
        'failure.sequenceWrong': 'Yanlış sıraya bastın!',
        'failure.sequenceTimeout': 'Sırayı zamanında tamamlayamadın!',
        'failure.evadeTimeout': 'Hedefi zamanında yakalayamadın!',
        'task.oddOneOut.name': 'Farklı Olan', 'task.oddOneOut.instruction': 'Diğerlerinden farklı olanı seç.',
        'task.extreme.name': 'Sayı Avı', 'task.extreme.largest': 'Sayısal değeri EN BÜYÜK olanı seç.', 'task.extreme.smallest': 'Sayısal değeri EN KÜÇÜK olanı seç.',
        'task.parity.name': 'Tek mi Çift mi?', 'task.parity.even': 'ÇİFT bir sayı seç.', 'task.parity.odd': 'TEK bir sayı seç.',
        'task.stroop.name': 'Renk Çatışması', 'task.stroop.word': 'YAZAN kelimeyi seç: {value}', 'task.stroop.ink': 'MÜREKKEP rengini seç: {value}', 'task.stroop.not': '{value} olmayan mürekkebi seç.',
        'task.memory.name': 'Önceki Cevap', 'task.memory.instruction': 'Önceki görevin hedef sayısını seç.',
        'task.flash.name': 'Flaş Hafıza', 'task.flash.watch': 'Diziyi ezberle: {sequence}', 'task.flash.repeat': 'Diziyi aynı sırayla tekrarla.',
        'task.alphabet.name': 'Alfabetik Sıra', 'task.alphabet.instruction': 'Harfleri alfabetik sırayla seç.',
        'task.clickPattern.name': 'Tek mi Çift mi Bas?', 'task.clickPattern.single': 'Yalnızca BİR kez bas.', 'task.clickPattern.double': 'Hızlıca İKİ kez bas.',
        'task.fakeButton.name': 'Gerçek Düğme', 'task.fakeButton.instruction': 'Kesintisiz çerçeveli GERÇEK düğmeyi seç.', 'task.fakeButton.real': 'GERÇEK', 'task.fakeButton.fake': 'SAHTE',
        'task.delayed.name': 'Son Sözü Dinle', 'task.delayed.initial': 'Bekle… İlk komut geçersiz.', 'task.delayed.final': 'ŞİMDİ {count} numaralı düğmeyi seç.',
        'task.positionMemory.name': 'Konum Hafızası', 'task.positionMemory.watch': 'Parlayan konumu ezberle.', 'task.positionMemory.choose': 'Önceki konumu seç.',
        'task.count.name': 'Sembol Sayacı', 'task.count.instruction': '{symbol} sembolünden kaç tane var?',
        'task.yesNo.name': 'Doğru mu?', 'task.yesNo.statement': '{number} sayısı {parity}.', 'task.yesNo.even': 'çifttir', 'task.yesNo.odd': 'tektir', 'common.yes': 'EVET', 'common.no': 'HAYIR',
        'failure.wrongChoice': 'Yanlış seçimi yaptın!', 'failure.gridTimeout': 'Seçim süresi bitti!', 'failure.flashEarly': 'Dizi henüz gösteriliyordu!', 'failure.clickSingle': 'Yalnızca bir kez basmalıydın!', 'failure.clickDouble': 'İkinci basış gelmedi!', 'failure.notActive': 'Etkinleşme işaretini beklemeliydin!',
        'feedback.success': 'DOĞRU!', 'feedback.almost': 'NEREDEYSE!', 'feedback.newMechanic': 'YENİ MEKANİK: {name}', 'feedback.paused': 'OYUN DURAKLATILDI', 'feedback.resumed': 'DEVAM!',
        'modifier.label': 'DEĞİŞTİRİCİ: {name}', 'modifier.mirrored': 'Aynalı komut', 'modifier.delayed': 'Gecikmeli kontroller', 'modifier.moving': 'Hareketli kontroller', 'modifier.shrinking': 'Küçülen kontroller', 'modifier.decoy': 'Sahte komut', 'modifier.swap': 'Konum değişimi', 'modifier.wait': 'İşareti bekle…', 'modifier.ready': 'HAZIR!', 'modifier.decoyText': 'SAHTE: İlk gördüğüne bas.', 'modifier.realText': 'GERÇEK KOMUT',
        'rule.label': 'KURAL ({remaining}): {name}', 'rule.announce': 'YENİ KURAL: {name}', 'rule.invert': 'EVET/HAYIR cevaplarını tersine çevir.', 'rule.ignoreRed': 'Kırmızı sahte hedefleri yok say.', 'rule.finalLine': 'Yalnızca SON komut satırı geçerlidir.', 'rule.oddWait': 'Tek seviyelerde etkinleşme işaretini bekle.', 'rule.emojiLiteral': 'Emojileri duygusal değil, gerçek anlamıyla yorumla.', 'rule.decoyLine': 'SAHTE SATIR: Rastgele bir düğmeye bas.',
        'access.choice': 'Seçenek {value}', 'access.memoryCell': 'Hafıza konumu {number}', 'color.red': 'kırmızı', 'color.blue': 'mavi', 'color.green': 'yeşil', 'color.yellow': 'sarı',
        'brand.tagline': 'HIZLI DÜŞÜN · DOĞRU BAS', 'meta.description': 'Reflex7, reflekslerini ve dikkatini sınayan sinir bozucu ama adil bir retro tarayıcı oyunudur.', 'meta.ogDescription': 'Hızlı düşün. Talimatı dikkatle oku. Doğru bas.',
        'menu.bestSummary': 'YAVAŞ  Seviye {slowLevel} · {slowScore} puan  /  HIZLI  Seviye {fastLevel} · {fastScore} puan',
        'audio.toggle': 'Sesi aç veya kapat', 'audio.on': 'SES: AÇIK', 'audio.off': 'SES: KAPALI',
        'game.score': 'PUAN: {score}', 'game.combo': 'KOMBO ×{combo}',
        'result.sessionEnded': 'OTURUM SONA ERDİ', 'result.player': 'Oyuncu: {nickname}', 'result.score': 'Puan: {score}', 'result.bestLevel': 'Mod rekoru: Seviye {level}', 'result.bestScore': 'En iyi puan: {score}', 'result.highestCombo': 'En yüksek kombo: ×{combo}', 'result.failedTask': 'Görev: {task} · {category}', 'result.failedContext': 'Etkin durum: {context}', 'result.noContext': 'Değiştirici veya genel kural yoktu.', 'result.menu': 'Ana Menü', 'result.newBest': 'YENİ KİŞİSEL REKOR!',
        'onboarding.howToButton': 'NASIL OYNANIR?', 'onboarding.title': 'NASIL OYNANIR', 'onboarding.body': 'Komutu hızlı ama dikkatli oku. Oyun aldatıcı olabilir; doğru cevap her zaman görünür ve uygulanabilirdir.', 'onboarding.instructions': 'Bazı yazılar tuzaktır. Yetkili komutu ve etkinleşme işaretini izle.', 'onboarding.controls': 'Fare, dokunma veya klavye kullan. P ya da Escape ile duraklat.', 'onboarding.local': 'Dil, ses ve mod rekorları yalnızca bu tarayıcıda saklanır.', 'onboarding.tipTitle': 'BİR ŞEYİ BİL', 'onboarding.tipBody': 'Reflex7 bazen seni yanıltır ama doğru girişini asla yok saymaz. Önce oku, sonra bas.', 'onboarding.tipDismiss': 'ANLADIM',
        'pause.button': 'Oyunu duraklat', 'pause.title': 'DURAKLATILDI', 'pause.body': 'Süre, görev gecikmeleri ve hareketler durdu. Hazır olduğunda devam et.', 'pause.resume': 'Devam Et',
        'discovery.modifier': 'DEĞİŞTİRİCİLER AÇILDI: Görev aynı, sunumu daha zor.', 'discovery.rule': 'GENEL KURAL: Bu kural birkaç görev boyunca geçerli kalır.',
        'sarcasm.1': 'Bunu görmüştün.', 'sarcasm.2': 'Refleks vardı, dikkat yoktu.', 'sarcasm.3': 'Talimat değişti. Sen değişmedin.', 'sarcasm.4': 'Neredeyse.', 'sarcasm.5': 'Bir tur daha?', 'feedback.successLate': 'Geç de olsa doğru.',
        'task.wait.name': 'Bekle', 'task.wait.before': 'ŞİMDİ BASMA', 'task.wait.go': 'ŞİMDİ BAS',
        'task.lastSecond.name': 'Son Saniye Talimatı', 'task.lastSecond.initial': '{color} DÜĞMEYE BAS', 'task.lastSecond.final': 'SON TALİMAT: {color} DÜĞMEYE BAS',
        'task.patience.name': 'Sabır Geri Sayımı', 'task.patience.instruction': 'Acele etme.', 'task.patience.go': 'GO!', 'task.patience.hesitation': '…',
        'failure.waitEarly': 'İzin verilmeden bastın!', 'failure.waitTimeout': 'ŞİMDİ BAS işaretinden sonra basmadın!',
        'failure.lastSecondTimeout': 'Son talimatı zamanında uygulamadın!', 'failure.patienceEarly': 'GO işareti görünmeden bastın!', 'failure.patienceTimeout': 'GO işaretinden sonra zamanında basmadın!',
        'color.purple': 'mor', 'color.orange': 'turuncu',
        'category.reaction': 'tepki', 'category.inhibition': 'kontrol', 'category.memory': 'hafıza', 'category.visual': 'görsel', 'category.arithmetic': 'matematik', 'category.sequence': 'sıralama', 'category.timing': 'zamanlama', 'category.language': 'dil', 'category.deception': 'aldatmaca', 'category.precision': 'hassasiyet', 'task.legacyName': 'Klasik görev', 'task.standard.name': 'Hızlı Basış', 'task.package.name': 'Kural Tuzağı', 'task.hold.name': 'Basılı Tut', 'task.colorShift.name': 'Renk Bekleme', 'task.sequence.name': 'Sıra', 'task.evade.name': 'Hareketli Hedef'
    },
    en: {
        'document.title': 'Reflex7 - Elite Edition v1.1.0',
        'menu.languageSelector': 'Language selection',
        'menu.turkish': 'Turkish',
        'menu.english': 'English',
        'menu.bestLevel': 'Best: Level {level}',
        'menu.nicknamePrompt': 'Enter a nickname for your local score and choose your speed:',
        'menu.nicknameLabel': 'NICKNAME:',
        'menu.nicknamePlaceholder': 'E.G. ALPHA-Z',
        'menu.slowMode': 'SLOW (7 sec)',
        'menu.fastMode': 'FAST (4 sec)',
        'menu.start': 'START',
        'game.level': 'LEVEL: {level}',
        'result.title': 'YOU ARE OUT!',
        'result.level': 'Level: {level}',
        'result.time': 'Time: {seconds} sec',
        'result.retry': 'Try Again',
        'localScore.title': 'YOUR LOCAL SCORE IS READY!',
        'localScore.details': '{nickname}: LEVEL {level}',
        'localScore.note': 'Your best level is stored only in this browser. No score is sent to a server.',
        'common.close': 'OK',
        'player.defaultNickname': 'PLAYER-Z',
        'task.quickPress': 'Press {count} times, quickly!',
        'task.futureWarning': 'Do NOT press on the next step!\nPress once now.',
        'task.doNotPressTrap': 'PRESS TWICE',
        'task.mathSubtract': 'Math: {value} - 2',
        'task.chainMore': 'Press one more time than before.',
        'task.blueRule': 'If you see blue, press once.',
        'task.fiveMinusTwo': '5 - 2',
        'task.blueWord': 'Blue',
        'task.doNotPress': 'DO NOT PRESS',
        'task.mouseRule': 'If you see a mouse, press 3 times!\nPress once now.',
        'task.threePlusOne': '3 + 1',
        'task.pressThree': 'PRESS PRESS PRESS',
        'task.mouseRuleAction': '🐭🐭\nFollow the mouse rule!',
        'task.mouseOverride': '🐭\nFollow the mouse rule!',
        'task.holdStart': 'PRESS AND HOLD!\nRelease when it turns green!',
        'task.holdWait': 'KEEP HOLDING...',
        'task.holdRelease': 'RELEASE!',
        'task.colorWait': 'PRESS ONLY\nwhen it turns green!',
        'task.evadeCatch': 'CATCH',
        'task.sequenceButton': 'Sequence button {number}',
        'task.evadeButton': 'Moving target, {remaining} presses left',
        'failure.shouldNotPress': 'You were not supposed to press!',
        'failure.tooManyPresses': 'You pressed too many times!',
        'failure.timeoutSlow': 'Time is up!\nYou were too slow.',
        'failure.earlyRelease': 'You released too early!',
        'failure.holdTimeout': 'You did not release before time ran out!',
        'failure.inputCancelled': 'The hold was interrupted!',
        'failure.wrongColor': 'You pressed on the wrong color!',
        'failure.colorTimeout': 'You did not press in time!',
        'failure.sequenceWrong': 'You pressed in the wrong order!',
        'failure.sequenceTimeout': 'You did not finish the sequence in time!',
        'failure.evadeTimeout': 'You did not catch the target in time!',
        'task.oddOneOut.name': 'Odd One Out', 'task.oddOneOut.instruction': 'Select the item that is different.',
        'task.extreme.name': 'Number Hunt', 'task.extreme.largest': 'Select the GREATEST numeric value.', 'task.extreme.smallest': 'Select the SMALLEST numeric value.',
        'task.parity.name': 'Odd or Even?', 'task.parity.even': 'Select an EVEN number.', 'task.parity.odd': 'Select an ODD number.',
        'task.stroop.name': 'Color Conflict', 'task.stroop.word': 'Select the WRITTEN word: {value}', 'task.stroop.ink': 'Select the INK color: {value}', 'task.stroop.not': 'Select ink that is not {value}.',
        'task.memory.name': 'Previous Answer', 'task.memory.instruction': 'Select the previous task’s target number.',
        'task.flash.name': 'Flash Memory', 'task.flash.watch': 'Memorize the sequence: {sequence}', 'task.flash.repeat': 'Repeat the sequence in the same order.',
        'task.alphabet.name': 'Alphabetical Order', 'task.alphabet.instruction': 'Select the letters in alphabetical order.',
        'task.clickPattern.name': 'Single or Double?', 'task.clickPattern.single': 'Press exactly ONCE.', 'task.clickPattern.double': 'Press TWICE, quickly.',
        'task.fakeButton.name': 'Real Button', 'task.fakeButton.instruction': 'Select the REAL button with the solid border.', 'task.fakeButton.real': 'REAL', 'task.fakeButton.fake': 'FAKE',
        'task.delayed.name': 'Listen to the Last Word', 'task.delayed.initial': 'Wait… The first command is invalid.', 'task.delayed.final': 'NOW select button {count}.',
        'task.positionMemory.name': 'Position Memory', 'task.positionMemory.watch': 'Memorize the glowing position.', 'task.positionMemory.choose': 'Select the previous position.',
        'task.count.name': 'Symbol Count', 'task.count.instruction': 'How many {symbol} symbols are there?',
        'task.yesNo.name': 'True or False?', 'task.yesNo.statement': '{number} is {parity}.', 'task.yesNo.even': 'even', 'task.yesNo.odd': 'odd', 'common.yes': 'YES', 'common.no': 'NO',
        'failure.wrongChoice': 'That was the wrong choice!', 'failure.gridTimeout': 'Selection time ran out!', 'failure.flashEarly': 'The sequence was still visible!', 'failure.clickSingle': 'You were supposed to press only once!', 'failure.clickDouble': 'The second press never arrived!', 'failure.notActive': 'You should have waited for the activation signal!',
        'feedback.success': 'CORRECT!', 'feedback.almost': 'ALMOST!', 'feedback.newMechanic': 'NEW MECHANIC: {name}', 'feedback.paused': 'GAME PAUSED', 'feedback.resumed': 'GO!',
        'modifier.label': 'MODIFIER: {name}', 'modifier.mirrored': 'Mirrored instruction', 'modifier.delayed': 'Delayed controls', 'modifier.moving': 'Moving controls', 'modifier.shrinking': 'Shrinking controls', 'modifier.decoy': 'Decoy instruction', 'modifier.swap': 'Position swap', 'modifier.wait': 'Wait for the signal…', 'modifier.ready': 'READY!', 'modifier.decoyText': 'DECOY: Press the first thing you see.', 'modifier.realText': 'REAL INSTRUCTION',
        'rule.label': 'RULE ({remaining}): {name}', 'rule.announce': 'NEW RULE: {name}', 'rule.invert': 'Reverse YES/NO answers.', 'rule.ignoreRed': 'Ignore red decoy targets.', 'rule.finalLine': 'Only the FINAL instruction line is valid.', 'rule.oddWait': 'On odd levels, wait for the activation signal.', 'rule.emojiLiteral': 'Interpret emojis literally, not emotionally.', 'rule.decoyLine': 'DECOY LINE: Press any button.',
        'access.choice': 'Choice {value}', 'access.memoryCell': 'Memory position {number}', 'color.red': 'red', 'color.blue': 'blue', 'color.green': 'green', 'color.yellow': 'yellow',
        'brand.tagline': 'THINK FAST · PRESS RIGHT', 'meta.description': 'Reflex7 is an irritating but fair retro browser game that tests reflexes and attention.', 'meta.ogDescription': 'Think fast. Read carefully. Press right.',
        'menu.bestSummary': 'SLOW  Level {slowLevel} · {slowScore} pts  /  FAST  Level {fastLevel} · {fastScore} pts',
        'audio.toggle': 'Turn sound on or off', 'audio.on': 'SOUND: ON', 'audio.off': 'SOUND: OFF',
        'game.score': 'SCORE: {score}', 'game.combo': 'COMBO ×{combo}',
        'result.sessionEnded': 'SESSION OVER', 'result.player': 'Player: {nickname}', 'result.score': 'Score: {score}', 'result.bestLevel': 'Mode best: Level {level}', 'result.bestScore': 'Best score: {score}', 'result.highestCombo': 'Highest combo: ×{combo}', 'result.failedTask': 'Task: {task} · {category}', 'result.failedContext': 'Active state: {context}', 'result.noContext': 'No modifier or global rule was active.', 'result.menu': 'Main Menu', 'result.newBest': 'NEW PERSONAL BEST!',
        'onboarding.howToButton': 'HOW TO PLAY?', 'onboarding.title': 'HOW TO PLAY', 'onboarding.body': 'Read quickly, but carefully. The game may deceive you; the correct answer is always visible and physically possible.', 'onboarding.instructions': 'Some text is a decoy. Follow the authoritative instruction and activation signal.', 'onboarding.controls': 'Use mouse, touch, or keyboard. Press P or Escape to pause.', 'onboarding.local': 'Language, sound, and mode records are stored only in this browser.', 'onboarding.tipTitle': 'ONE THING FIRST', 'onboarding.tipBody': 'Reflex7 may mislead you, but it never ignores a valid input. Read first, then press.', 'onboarding.tipDismiss': 'GOT IT',
        'pause.button': 'Pause the game', 'pause.title': 'PAUSED', 'pause.body': 'The timer, task delays, and movement are frozen. Resume when you are ready.', 'pause.resume': 'Resume',
        'discovery.modifier': 'MODIFIERS UNLOCKED: Same task, harder presentation.', 'discovery.rule': 'GLOBAL RULE: This rule stays active for several tasks.',
        'sarcasm.1': 'You saw that.', 'sarcasm.2': 'Fast hands, slow attention.', 'sarcasm.3': 'The instruction changed. You did not.', 'sarcasm.4': 'Almost.', 'sarcasm.5': 'One more run?', 'feedback.successLate': 'Correct. Eventually.',
        'task.wait.name': 'Wait', 'task.wait.before': 'DO NOT PRESS YET', 'task.wait.go': 'PRESS NOW',
        'task.lastSecond.name': 'Last Second Instruction', 'task.lastSecond.initial': 'PRESS THE {color} BUTTON', 'task.lastSecond.final': 'FINAL INSTRUCTION: PRESS THE {color} BUTTON',
        'task.patience.name': 'Patience Countdown', 'task.patience.instruction': 'Be patient.', 'task.patience.go': 'GO!', 'task.patience.hesitation': '…',
        'failure.waitEarly': 'You pressed before permission!', 'failure.waitTimeout': 'You did not press after PRESS NOW appeared!',
        'failure.lastSecondTimeout': 'You did not follow the final instruction in time!', 'failure.patienceEarly': 'You pressed before GO appeared!', 'failure.patienceTimeout': 'You did not press after GO appeared!',
        'color.purple': 'purple', 'color.orange': 'orange',
        'category.reaction': 'reaction', 'category.inhibition': 'inhibition', 'category.memory': 'memory', 'category.visual': 'visual', 'category.arithmetic': 'arithmetic', 'category.sequence': 'sequence', 'category.timing': 'timing', 'category.language': 'language', 'category.deception': 'deception', 'category.precision': 'precision', 'task.legacyName': 'Classic task', 'task.standard.name': 'Rapid Press', 'task.package.name': 'Rule Trap', 'task.hold.name': 'Hold and Release', 'task.colorShift.name': 'Color Wait', 'task.sequence.name': 'Sequence', 'task.evade.name': 'Moving Target'
    }
};

function safeStorageGet(key) {
    try {
        return window.localStorage.getItem(key);
    } catch (error) {
        return null;
    }
}

function safeStorageSet(key, value) {
    try {
        window.localStorage.setItem(key, String(value));
        return true;
    } catch (error) {
        return false;
    }
}

function clamp(value, minimum, maximum) {
    return Math.min(maximum, Math.max(minimum, value));
}

const storedLanguage = safeStorageGet(STORAGE_KEYS.language);
let currentLanguage = Object.hasOwn(translations, storedLanguage) ? storedLanguage : 'tr';
const readStoredNumber = (key) => {
    const value = Number.parseInt(safeStorageGet(key), 10);
    return Number.isFinite(value) && value >= 0 ? value : 0;
};
const legacyBestLevel = readStoredNumber(STORAGE_KEYS.legacyBestLevel);
['7', '4'].forEach((mode) => {
    if (safeStorageGet(STORAGE_KEYS.bestLevel(mode)) === null && legacyBestLevel > 0) safeStorageSet(STORAGE_KEYS.bestLevel(mode), legacyBestLevel);
});
const modeRecords = {
    '7': { level: readStoredNumber(STORAGE_KEYS.bestLevel('7')), score: readStoredNumber(STORAGE_KEYS.bestScore('7')) },
    '4': { level: readStoredNumber(STORAGE_KEYS.bestLevel('4')), score: readStoredNumber(STORAGE_KEYS.bestScore('4')) }
};
let soundEnabled = safeStorageGet(STORAGE_KEYS.sound) !== 'false';
let tipSeen = safeStorageGet(STORAGE_KEYS.tipSeen) === 'true';
let storedDiscoveries = [];
try {
    const parsedDiscoveries = JSON.parse(safeStorageGet(STORAGE_KEYS.discoveries) || '[]');
    if (Array.isArray(parsedDiscoveries)) storedDiscoveries = parsedDiscoveries.filter((value) => typeof value === 'string');
} catch (error) {
    storedDiscoveries = [];
}

let level = 1;
let currentClicks = 0;
let targetClicks = 0;
let baseTime = 7.0;
let timeLeft = 7.0;
let timerInterval = null;
let timerDeadline = 0;
let timerDuration = 0;
let gameActive = false;
let startTime = 0;
let nickname = '';
let finalResult = null;
let selectedMode = '7';
let sessionScore = 0;
let combo = 0;
let highestCombo = 0;
let sessionPersonalBest = false;
let personalBestAnnounced = false;
let failedTaskSnapshot = null;
let inputTransitionLocked = false;
let levelTransitionTimer = null;
let menuStartTimer = null;
let levelTransitionDeadline = 0;
let levelTransitionRemaining = 0;
let pauseReason = null;
let urgencyStage = 0;
let pausedStartedAt = 0;
let totalPausedMs = 0;

let packageQueue = [];
let lastTarget = 0;
let legacyRule = null;
let activeTaskObj = null;
let currentInstruction = { key: 'menu.start', params: {} };
let activeTaskDefinition = null;
let activeModifiers = [];
let activeGlobalRule = null;
let taskHistory = [];
let categoryHistory = [];
let taskMemory = {};
let completedTaskCount = 0;
let gameplayPaused = false;
let announcementTimer = null;
const seenMechanics = new Set(storedDiscoveries.filter((value) => value.startsWith('task:')).map((value) => value.slice(5)));
const DEBUG_TASK_ENGINE = false;

class RetroAudio {
    constructor() {
        this.context = null;
        this.channels = new Map();
        this.lastPlayed = new Map();
        this.unlocked = false;
    }

    unlock() {
        if (!soundEnabled || this.unlocked) return;
        const AudioContextClass = window.AudioContext || window.webkitAudioContext;
        if (!AudioContextClass) return;
        try {
            this.context ||= new AudioContextClass();
            this.context.resume?.();
            this.unlocked = true;
        } catch (error) {
            this.context = null;
        }
    }

    play(type) {
        if (!soundEnabled || !this.unlocked || !this.context) return false;
        const presets = {
            menu: [320, 0.045, 0.025], start: [440, 0.08, 0.035], success: [660, 0.055, 0.03],
            fail: [145, 0.12, 0.04], level: [520, 0.045, 0.025], rule: [260, 0.1, 0.032],
            modifier: [390, 0.065, 0.026], urgency: [190, 0.035, 0.02], best: [880, 0.14, 0.035],
            signal: [740, 0.03, 0.014]
        };
        const preset = presets[type];
        if (!preset) return false;
        const nowMs = performance.now();
        const cooldown = type === 'urgency' ? 420 : 70;
        if (nowMs - (this.lastPlayed.get(type) || -Infinity) < cooldown) return false;
        this.lastPlayed.set(type, nowMs);
        this.stopChannel(type === 'urgency' ? 'urgency' : 'feedback');
        const [frequency, duration, volume] = preset;
        const oscillator = this.context.createOscillator();
        const gain = this.context.createGain();
        oscillator.type = 'square'; oscillator.frequency.setValueAtTime(frequency, this.context.currentTime);
        gain.gain.setValueAtTime(0.0001, this.context.currentTime);
        gain.gain.exponentialRampToValueAtTime(volume, this.context.currentTime + 0.008);
        gain.gain.exponentialRampToValueAtTime(0.0001, this.context.currentTime + duration);
        oscillator.connect(gain); gain.connect(this.context.destination);
        oscillator.start(); oscillator.stop(this.context.currentTime + duration + 0.01);
        const channel = type === 'urgency' ? 'urgency' : 'feedback';
        this.channels.set(channel, oscillator);
        oscillator.onended = () => { if (this.channels.get(channel) === oscillator) this.channels.delete(channel); };
        return true;
    }

    stopChannel(channel) {
        const oscillator = this.channels.get(channel);
        if (oscillator) { try { oscillator.stop(); } catch (error) { /* Already stopped. */ } }
        this.channels.delete(channel);
    }

    stopAll() {
        [...this.channels.keys()].forEach((channel) => this.stopChannel(channel));
    }

    setEnabled(enabled) {
        soundEnabled = Boolean(enabled);
        safeStorageSet(STORAGE_KEYS.sound, soundEnabled);
        if (!soundEnabled) this.stopAll();
        else this.unlock();
        renderSoundControls();
    }
}

const audio = new RetroAudio();

const stage = document.getElementById('stage');
const nicknameInput = document.getElementById('nickname-input');
const mainButton = document.getElementById('main-button');
const instructionText = document.getElementById('instruction');
const levelDisplay = document.getElementById('level-display');
const highScoreDisplay = document.getElementById('high-score-display');
const timerBar = document.getElementById('timer-bar');
const sequenceContainer = document.getElementById('sequence-container');
const arenaInstruction = document.getElementById('arena-instruction');
const decoyInstruction = document.getElementById('decoy-instruction');
const announcement = document.getElementById('announcement');
const globalRuleBadge = document.getElementById('global-rule-badge');
const modifierIndicator = document.getElementById('modifier-indicator');
const gameOverScreen = document.getElementById('game-over-screen');
const pauseScreen = document.getElementById('pause-screen');
const firstRunTip = document.getElementById('first-run-tip');
const failMessage = document.getElementById('fail-message');
const resultPlayerStat = document.getElementById('result-player-stat');
const finalLevelStat = document.getElementById('final-level-stat');
const finalScoreStat = document.getElementById('final-score-stat');
const bestLevelStat = document.getElementById('best-level-stat');
const bestScoreStat = document.getElementById('best-score-stat');
const totalTimeStat = document.getElementById('total-time-stat');
const highestComboStat = document.getElementById('highest-combo-stat');
const failedTaskStat = document.getElementById('failed-task-stat');
const failedContextStat = document.getElementById('failed-context-stat');
const newBestResult = document.getElementById('new-best-result');
const sarcasmMessage = document.getElementById('sarcasm-message');
const scoreDisplay = document.getElementById('score-display');
const comboDisplay = document.getElementById('combo-display');
const pauseButton = document.getElementById('pause-button');

function t(key, params = {}) {
    const dictionary = translations[currentLanguage] || translations.tr;
    const template = dictionary[key] ?? translations.tr[key] ?? key;
    return template.replace(/\{(\w+)\}/g, (match, name) => (
        Object.hasOwn(params, name) ? String(params[name]) : match
    ));
}

function setInstruction(key, params = {}) {
    currentInstruction = { key, params };
    const text = t(key, params);
    instructionText.textContent = text;
    arenaInstruction.textContent = text;
    mainButton.setAttribute('aria-label', text.replace(/\n/g, ' '));
}

function formatNumber(value) {
    return Number(value || 0).toLocaleString(currentLanguage === 'tr' ? 'tr-TR' : 'en-US');
}

function renderSessionHud() {
    levelDisplay.textContent = t('game.level', { level });
    scoreDisplay.textContent = t('game.score', { score: formatNumber(sessionScore) });
    comboDisplay.textContent = combo >= 2 ? t('game.combo', { combo }) : '';
    comboDisplay.classList.toggle('visible', combo >= 2);
}

function taskDisplayName(snapshot = failedTaskSnapshot) {
    return snapshot?.nameKey ? t(snapshot.nameKey) : t('task.legacyName');
}

function failureContextText(snapshot = failedTaskSnapshot) {
    if (!snapshot) return '';
    const parts = snapshot.modifiers.map((id) => t(MODIFIER_DEFINITIONS[id].nameKey));
    if (snapshot.ruleNameKey) parts.push(t(snapshot.ruleNameKey));
    return parts.join(' + ');
}

function renderSoundControls() {
    document.querySelectorAll('.sound-toggle').forEach((button) => {
        button.textContent = t(soundEnabled ? 'audio.on' : 'audio.off');
        button.setAttribute('aria-pressed', String(soundEnabled));
    });
}

function renderDynamicTranslations() {
    highScoreDisplay.textContent = t('menu.bestSummary', {
        slowLevel: modeRecords['7'].level, slowScore: formatNumber(modeRecords['7'].score),
        fastLevel: modeRecords['4'].level, fastScore: formatNumber(modeRecords['4'].score)
    });
    renderSessionHud();
    setInstruction(currentInstruction.key, currentInstruction.params);

    if (finalResult) {
        failMessage.textContent = t(finalResult.failureKey, finalResult.failureParams);
        resultPlayerStat.textContent = t('result.player', { nickname: finalResult.nickname });
        finalLevelStat.textContent = t('result.level', { level: finalResult.level });
        finalScoreStat.textContent = t('result.score', { score: formatNumber(finalResult.score) });
        bestLevelStat.textContent = t('result.bestLevel', { level: modeRecords[selectedMode].level });
        bestScoreStat.textContent = t('result.bestScore', { score: formatNumber(modeRecords[selectedMode].score) });
        totalTimeStat.textContent = t('result.time', { seconds: finalResult.seconds });
        highestComboStat.textContent = t('result.highestCombo', { combo: finalResult.highestCombo });
        failedTaskStat.textContent = t('result.failedTask', { task: taskDisplayName(), category: t(`category.${failedTaskSnapshot?.category || 'reaction'}`) });
        const context = failureContextText();
        failedContextStat.textContent = context ? t('result.failedContext', { context }) : t('result.noContext');
        newBestResult.textContent = finalResult.newBest ? t('result.newBest') : '';
        sarcasmMessage.textContent = t(`sarcasm.${finalResult.sarcasm}`);
    }

    if (activeTaskObj && typeof activeTaskObj.localize === 'function') {
        activeTaskObj.localize();
    }
    renderGameplayIndicators();
    renderSoundControls();
}

function applyTranslations() {
    document.documentElement.lang = currentLanguage;
    document.title = t('document.title');
    document.querySelector?.('meta[name="description"]')?.setAttribute('content', t('meta.description'));
    document.querySelector?.('meta[property="og:description"]')?.setAttribute('content', t('meta.ogDescription'));
    document.querySelector?.('meta[name="twitter:description"]')?.setAttribute('content', t('meta.description'));
    document.querySelector?.('meta[property="og:locale"]')?.setAttribute('content', currentLanguage === 'tr' ? 'tr_TR' : 'en_US');

    document.querySelectorAll('[data-i18n]').forEach((element) => {
        element.textContent = t(element.dataset.i18n);
    });
    document.querySelectorAll('[data-i18n-placeholder]').forEach((element) => {
        element.setAttribute('placeholder', t(element.dataset.i18nPlaceholder));
    });
    document.querySelectorAll('[data-i18n-aria-label]').forEach((element) => {
        element.setAttribute('aria-label', t(element.dataset.i18nAriaLabel));
    });
    document.querySelectorAll('.language-btn').forEach((button) => {
        const isActive = button.dataset.language === currentLanguage;
        button.classList.toggle('active', isActive);
        button.setAttribute('aria-pressed', String(isActive));
    });

    renderDynamicTranslations();
}

document.querySelectorAll('.language-btn').forEach((button) => {
    button.addEventListener('click', () => {
        audio.unlock(); audio.play('menu');
        const requestedLanguage = button.dataset.language;
        if (!Object.hasOwn(translations, requestedLanguage)) return;
        currentLanguage = requestedLanguage;
        safeStorageSet(STORAGE_KEYS.language, currentLanguage);
        applyTranslations();
    });
});

function triggerShake() {
    document.body.classList.remove('shake');
    void document.body.offsetWidth;
    document.body.classList.add('shake');
    window.setTimeout(() => document.body.classList.remove('shake'), 400);
}

document.querySelectorAll('.mode-btn').forEach((button) => {
    button.addEventListener('click', () => {
        if (menuStartTimer !== null || gameActive) return;
        audio.unlock(); audio.play('menu');
        const enteredNickname = nicknameInput.value.trim().slice(0, NICKNAME_MAX_LENGTH);
        nickname = (enteredNickname || t('player.defaultNickname')).toLocaleUpperCase(currentLanguage);
        baseTime = Number.parseFloat(button.dataset.time);
        selectedMode = baseTime === 4 ? '4' : '7';
        stage.classList.add('is-playing');
        document.querySelectorAll('.mode-btn').forEach((modeButton) => { modeButton.disabled = true; });
        menuStartTimer = window.setTimeout(() => {
            menuStartTimer = null;
            document.querySelectorAll('.mode-btn').forEach((modeButton) => { modeButton.disabled = false; });
            startGame();
        }, 800);
    });
});

function clearTimer() {
    if (timerInterval !== null) {
        window.clearInterval(timerInterval);
        timerInterval = null;
    }
}

function startGame() {
    level = 1;
    gameActive = true;
    startTime = performance.now();
    packageQueue = [];
    legacyRule = null;
    lastTarget = 0;
    activeGlobalRule = null;
    taskHistory = [];
    categoryHistory = [];
    taskMemory = {};
    completedTaskCount = 0;
    gameplayPaused = false;
    sessionScore = 0;
    combo = 0;
    highestCombo = 0;
    sessionPersonalBest = false;
    personalBestAnnounced = false;
    finalResult = null;
    failedTaskSnapshot = null;
    inputTransitionLocked = false;
    totalPausedMs = 0;
    pausedStartedAt = 0;
    gameOverScreen.classList.remove('active');
    pauseScreen.classList.remove('active');
    renderSessionHud();
    audio.play('start');
    nextLevel();
}

function calculateTaskScore() {
    const normalizedTime = timerDuration > 0 ? clamp(timeLeft / timerDuration, 0, 1) : 0;
    const difficulty = activeTaskDefinition?.difficulty || 1;
    const basePoints = 100 + (level * 5) + (difficulty * 25) + (activeModifiers.length * 20) + (activeGlobalRule ? 15 : 0) + Math.floor(normalizedTime * 100);
    const comboMultiplier = 1 + (Math.min(Math.max(combo - 1, 0), 10) * 0.05);
    return Math.round(basePoints * comboMultiplier);
}

function persistModeRecords() {
    safeStorageSet(STORAGE_KEYS.bestLevel(selectedMode), modeRecords[selectedMode].level);
    safeStorageSet(STORAGE_KEYS.bestScore(selectedMode), modeRecords[selectedMode].score);
}

function updatePersonalBest(reachedLevel = level) {
    const record = modeRecords[selectedMode];
    const improved = reachedLevel > record.level || sessionScore > record.score;
    if (reachedLevel > record.level) record.level = reachedLevel;
    if (sessionScore > record.score) record.score = sessionScore;
    if (improved) {
        sessionPersonalBest = true;
        persistModeRecords();
        if (!personalBestAnnounced) {
            personalBestAnnounced = true;
            showAnnouncement('result.newBest', {}, 900);
            document.body.classList.add('new-best-flash');
            window.setTimeout(() => document.body.classList.remove('new-best-flash'), 650);
            audio.play('best');
        }
    }
}

function taskSuccess() {
    if (!gameActive) return;
    if (activeTaskObj && typeof activeTaskObj.getMemory === 'function') {
        taskMemory = { ...taskMemory, ...activeTaskObj.getMemory() };
    }
    completedTaskCount += 1;
    combo += 1;
    highestCombo = Math.max(highestCombo, combo);
    sessionScore += calculateTaskScore();
    updatePersonalBest(level + 1);
    if (activeGlobalRule) {
        activeGlobalRule.remaining -= 1;
        if (activeGlobalRule.remaining <= 0) activeGlobalRule = null;
    }
    flashFeedback('success');
    audio.play('success');
    level += 1;
    renderSessionHud();
    nextLevel();
}

function taskFail(failureKey, failureParams = {}) {
    if (!gameActive) return;
    combo = 0;
    triggerShake();
    audio.play('fail');
    gameOver(failureKey, failureParams);
}

function resetInputState() {
    resetPointerInput();
    keyboardTask = null;
}

function nextLevel() {
    clearTimer();
    window.clearTimeout(levelTransitionTimer);
    resetInputState();
    if (activeTaskObj && typeof activeTaskObj.cleanup === 'function') {
        activeTaskObj.cleanup();
    }
    activeTaskObj = null;
    activeTaskDefinition = null;
    activeModifiers = [];
    currentClicks = 0;

    const speedFactor = Math.max(0.4, 1 - (Math.floor(level / 5) * 0.05));
    timeLeft = baseTime * speedFactor;
    renderSessionHud();

    mainButton.style.display = 'flex';
    mainButton.className = '';
    mainButton.style.backgroundColor = '#4CAF50';
    instructionText.style.color = 'white';
    sequenceContainer.style.display = 'none';
    sequenceContainer.replaceChildren();
    arenaInstruction.style.display = 'none';
    arenaInstruction.className = '';
    arenaInstruction.removeAttribute('data-authority');
    decoyInstruction.style.display = 'none';
    decoyInstruction.textContent = '';
    modifierIndicator.textContent = '';
    timerBar.style.strokeDashoffset = '0';

    let startedGlobalRule = false;
    if (packageQueue.length > 0) {
        activeTaskDefinition = { id: 'package-step', category: 'deception', difficulty: 3, nameKey: 'task.package.name' };
        activeTaskObj = new TaskStandard(packageQueue.shift());
    } else {
        startedGlobalRule = prepareGlobalRule();
        activeTaskDefinition = selectTaskDefinition();
        activeTaskObj = activeTaskDefinition.create();
        activeModifiers = selectModifiers(activeTaskDefinition);
        rememberSelection(activeTaskDefinition);
        if (DEBUG_TASK_ENGINE) console.info('[Reflex7 Task Engine]', { task: activeTaskDefinition.id, category: activeTaskDefinition.category, modifiers: activeModifiers, duration: timeLeft, weight: activeTaskDefinition.selectionWeight });
    }

    activeTaskObj.setup();
    let transitionDelay = 140;
    if (!startedGlobalRule) transitionDelay = Math.max(transitionDelay, announceNewMechanic(activeTaskDefinition));
    applyGlobalRuleToTask(activeTaskObj);
    applyModifiers(activeTaskObj, activeModifiers);
    renderGameplayIndicators();
    if (activeModifiers.length && !storedDiscoveries.includes('system:modifiers')) {
        storeDiscovery('system:modifiers'); showAnnouncement('discovery.modifier', {}, 1100); transitionDelay = Math.max(transitionDelay, 1150);
    }
    if (activeModifiers.length) audio.play('modifier');
    if (startedGlobalRule) {
        if (!storedDiscoveries.includes('system:rules')) { storeDiscovery('system:rules'); showAnnouncement('discovery.rule', {}, 1250); transitionDelay = Math.max(transitionDelay, 1300); }
        else showAnnouncement('rule.announce', { name: t(activeGlobalRule.nameKey) }, 950);
        audio.play('rule');
    }
    if (level > 8 && Math.random() < 0.08) document.body.classList.add('cursor-decoy');
    inputTransitionLocked = true;
    document.body.classList.add('task-transition');
    pauseTaskTimeouts(activeTaskObj);
    armTaskTransition(transitionDelay);
}

function armTaskTransition(delay) {
    window.clearTimeout(levelTransitionTimer);
    levelTransitionRemaining = delay;
    levelTransitionDeadline = performance.now() + delay;
    levelTransitionTimer = window.setTimeout(finishTaskTransition, delay);
}

function finishTaskTransition() {
    levelTransitionTimer = null;
    levelTransitionRemaining = 0;
    if (!gameActive || !activeTaskObj || gameplayPaused) return;
    resumeTaskTimeouts(activeTaskObj);
    inputTransitionLocked = false;
    document.body.classList.remove('task-transition', 'cursor-decoy');
    urgencyStage = 0;
    audio.play('level');
    startTimer();
}

function generateMegaTask() {
    activeTaskDefinition = selectTaskDefinition();
    activeTaskObj = activeTaskDefinition.create();
}

function startRandomPackage() {
    const randomValue = Math.random();

    if (randomValue < 0.25) {
        packageQueue = [
            { textKey: 'task.futureWarning', target: 1 },
            { textKey: 'task.doNotPressTrap', target: 0, buttonColor: '#f44336' }
        ];
    } else if (randomValue < 0.5) {
        const start = Math.floor(Math.random() * 3) + 1;
        packageQueue = [
            { textKey: 'task.mathSubtract', textParams: { value: start + 2 }, target: start },
            { textKey: 'task.chainMore', type: 'chain_math' },
            { textKey: 'task.chainMore', type: 'chain_math' }
        ];
    } else if (randomValue < 0.75) {
        packageQueue = [
            { textKey: 'task.blueRule', buttonColor: '#2196F3', target: 1, setRule: 'blue_rule' },
            { textKey: 'task.fiveMinusTwo', target: 3 },
            { textKey: 'task.blueWord', target: 0, fontColor: 'white', buttonColor: '#2196F3' },
            { textKey: 'task.doNotPress', target: 1, fontColor: '#2196F3' }
        ];
    } else {
        packageQueue = [
            { textKey: 'task.mouseRule', target: 1, setRule: 'mouse_rule' },
            { textKey: 'task.threePlusOne', target: 4 },
            { textKey: 'task.pressThree', target: 3 },
            { textKey: 'task.mouseRuleAction', target: 3 }
        ];
    }
}

function startTimer() {
    timerDuration = timeLeft;
    timerDeadline = performance.now() + (timerDuration * 1000);

    const updateTimer = () => {
        if (!gameActive || timerInterval === null) return;

        const taskAtStartOfTick = activeTaskObj;
        timeLeft = Math.max(0, (timerDeadline - performance.now()) / 1000);
        const progress = timerDuration > 0 ? timeLeft / timerDuration : 0;
        timerBar.style.strokeDashoffset = String(TOTAL_TIMER_DASH * (1 - progress));
        const nextUrgency = progress <= 0.15 ? 2 : progress <= 0.3 ? 1 : 0;
        if (nextUrgency > urgencyStage) { urgencyStage = nextUrgency; audio.play('urgency'); }
        document.body.classList.toggle('timer-urgent', nextUrgency >= 1);
        document.body.classList.toggle('timer-critical', nextUrgency >= 2);

        if (taskAtStartOfTick && typeof taskAtStartOfTick.updateTick === 'function') {
            taskAtStartOfTick.updateTick(timeLeft, timerDuration);
        }

        if (!gameActive || activeTaskObj !== taskAtStartOfTick) return;
        if (timeLeft > 0) return;

        clearTimer();
        taskAtStartOfTick.onTimeUp();
    };

    timerInterval = window.setInterval(updateTimer, 50);
    updateTimer();
}

// ---------- TASK ENGINE V2: SHARED UTILITIES ----------

function randomInt(minimum, maximum) {
    return Math.floor(Math.random() * ((maximum - minimum) + 1)) + minimum;
}

function shuffled(values) {
    const copy = [...values];
    for (let index = copy.length - 1; index > 0; index -= 1) {
        const swapIndex = randomInt(0, index);
        [copy[index], copy[swapIndex]] = [copy[swapIndex], copy[index]];
    }
    return copy;
}

function scheduleTaskTimeout(task, callback, delay) {
    task._trackedTimeouts ||= new Set();
    const record = { callback, remaining: delay, started: performance.now(), id: null, paused: false };
    const arm = () => {
        record.started = performance.now();
        record.paused = false;
        record.id = window.setTimeout(() => {
            task._trackedTimeouts.delete(record);
            if (gameActive && activeTaskObj === task) callback();
        }, record.remaining);
    };
    record.arm = arm;
    task._trackedTimeouts.add(record);
    arm();
    return record;
}

function clearTaskTimeouts(task) {
    if (!task._trackedTimeouts) return;
    task._trackedTimeouts.forEach((record) => window.clearTimeout(record.id));
    task._trackedTimeouts.clear();
}

function pauseTaskTimeouts(task) {
    if (!task._trackedTimeouts) return;
    task._trackedTimeouts.forEach((record) => {
        if (record.paused) return;
        window.clearTimeout(record.id);
        record.remaining = Math.max(0, record.remaining - (performance.now() - record.started));
        record.paused = true;
    });
}

function resumeTaskTimeouts(task) {
    if (!task._trackedTimeouts) return;
    task._trackedTimeouts.forEach((record) => { if (record.paused) record.arm(); });
}

function currentDifficultyBand() {
    if (level <= 5) return { id: 'onboarding', options: 4, memory: 3, modifiers: 0, globalChance: 0 };
    if (level <= 12) return { id: 'basic', options: 5, memory: 3, modifiers: 0, globalChance: 0 };
    if (level <= 24) return { id: 'mixed', options: 6, memory: 4, modifiers: 0, globalChance: 0.08 };
    if (level <= 39) return { id: 'modified', options: 7, memory: 4, modifiers: 1, globalChance: 0.12 };
    return { id: 'advanced', options: 9, memory: 5, modifiers: 2, globalChance: 0.17 };
}

class GridTaskBase {
    constructor() {
        this.inputLocked = false;
        this.grid = null;
    }

    setupGrid(instructionKey, params = {}, columns = 3) {
        mainButton.style.display = 'none';
        arenaInstruction.style.display = 'block';
        sequenceContainer.style.display = 'block';
        setInstruction(instructionKey, params);
        this.grid = document.createElement('div');
        this.grid.className = 'choice-grid';
        this.grid.style.setProperty('--grid-columns', String(columns));
        sequenceContainer.appendChild(this.grid);
    }

    addChoice(label, isCorrect, options = {}) {
        const button = document.createElement('button');
        button.type = 'button';
        button.className = `task-choice ${options.className || ''}`.trim();
        button.textContent = String(label);
        button.dataset.correct = String(Boolean(isCorrect));
        button.setAttribute('aria-label', options.ariaLabel || t('access.choice', { value: label }));
        if (options.fontSize) button.style.fontSize = options.fontSize;
        if (options.color) button.style.color = options.color;
        button.addEventListener('click', () => {
            if (!gameActive || gameplayPaused || inputTransitionLocked || activeTaskObj !== this) return;
            if (this.inputLocked) {
                if (this.failWhenLocked) taskFail('failure.notActive');
                return;
            }
            this.onChoice(button, isCorrect, options);
        });
        this.grid.appendChild(button);
        return button;
    }

    onChoice(button, isCorrect) {
        if (isCorrect) taskSuccess();
        else taskFail('failure.wrongChoice');
    }

    getModifierTargets() {
        return this.grid ? [...this.grid.querySelectorAll('.task-choice')] : [];
    }

    setInputLocked(locked, reason = 'task') {
        this.inputLocks ||= new Set();
        if (locked) this.inputLocks.add(reason);
        else this.inputLocks.delete(reason);
        this.inputLocked = this.inputLocks.size > 0;
        this.getModifierTargets().forEach((button) => {
            button.setAttribute('aria-disabled', String(this.inputLocked));
        });
    }

    markRedDecoy() {
        const decoys = this.getModifierTargets().filter((button) => button.dataset.correct !== 'true');
        if (decoys.length) decoys[randomInt(0, decoys.length - 1)].classList.add('red-decoy');
    }

    pause() { pauseTaskTimeouts(this); }
    resume() { resumeTaskTimeouts(this); }
    onTimeUp() { taskFail('failure.gridTimeout'); }

    cleanup() {
        clearTaskTimeouts(this);
        sequenceContainer.replaceChildren();
        sequenceContainer.style.display = 'none';
        arenaInstruction.style.display = 'none';
        mainButton.style.display = 'flex';
    }
}

class TaskStandard {
    constructor(config) {
        this.config = config;
    }

    setup() {
        this.target = this.config.type === 'chain_math' ? lastTarget + 1 : this.config.target;
        this.textKey = this.config.textKey;
        this.textParams = this.config.textParams || {};

        if (legacyRule === 'mouse_rule' && !this.config.setRule && Math.random() < 0.2) {
            this.target = 3;
            this.textKey = 'task.mouseOverride';
            this.textParams = {};
        }

        targetClicks = this.target;
        setInstruction(this.textKey, this.textParams);
        instructionText.style.color = this.config.fontColor || 'white';
        mainButton.style.backgroundColor = this.config.buttonColor || '#4CAF50';
        if (this.config.setRule) legacyRule = this.config.setRule;
        lastTarget = targetClicks;
    }

    handleActivate() {
        currentClicks += 1;
        if (targetClicks === 0 || currentClicks > targetClicks) {
            taskFail(targetClicks === 0 ? 'failure.shouldNotPress' : 'failure.tooManyPresses');
            return;
        }
        if (currentClicks === targetClicks) taskSuccess();
    }

    onTimeUp() {
        if (targetClicks === 0) taskSuccess();
        else taskFail('failure.timeoutSlow');
    }

    getMemory() {
        return { previousTarget: targetClicks, previousClickCount: targetClicks };
    }

    cleanup() {}
}

class TaskHoldRelease {
    setup() {
        this.requiredHoldTime = clamp(timeLeft * 0.45, 0.45, 1.0);
        this.isDown = false;
        this.downStartedAt = 0;
        this.readyToRelease = false;
        setInstruction('task.holdStart');
        mainButton.style.backgroundColor = '#ff9800';
        targetClicks = 1;
        lastTarget = 1;
    }

    handlePressStart() {
        if (this.isDown) return;
        this.isDown = true;
        this.downStartedAt = performance.now();
        setInstruction('task.holdWait');
    }

    handlePressEnd() {
        if (!this.isDown) return;
        const holdDuration = (performance.now() - this.downStartedAt) / 1000;
        this.isDown = false;
        if (holdDuration >= this.requiredHoldTime) taskSuccess();
        else taskFail('failure.earlyRelease');
    }

    handlePressCancel() {
        if (!this.isDown) return;
        this.isDown = false;
        taskFail('failure.inputCancelled');
    }

    updateTick() {
        if (!this.isDown || this.readyToRelease) return;
        const holdDuration = (performance.now() - this.downStartedAt) / 1000;
        if (holdDuration >= this.requiredHoldTime) {
            this.readyToRelease = true;
            mainButton.style.backgroundColor = '#4CAF50';
            setInstruction('task.holdRelease');
        }
    }

    onTimeUp() {
        taskFail('failure.holdTimeout');
    }

    cleanup() {
        this.isDown = false;
    }

    pause() { this.pausedAt = performance.now(); }
    resume() { if (this.isDown && this.pausedAt) this.downStartedAt += performance.now() - this.pausedAt; }
}

class TaskColorShift {
    setup() {
        setInstruction('task.colorWait');
        this.currentColor = '#f44336';
        mainButton.style.backgroundColor = this.currentColor;

        const responseWindow = clamp(timeLeft * 0.32, 0.35, 0.8);
        const maximumDelay = Math.min(2.0, Math.max(0.2, timeLeft - responseWindow));
        const preferredMinimum = Math.max(0.35, Math.min(1.0, timeLeft * 0.3));
        const minimumDelay = Math.min(maximumDelay, preferredMinimum);
        const colorDelay = minimumDelay + (Math.random() * (maximumDelay - minimumDelay));
        this.responseWindow = responseWindow;
        this.colorDelay = colorDelay;

        this.colorTimer = scheduleTaskTimeout(this, () => {
            if (gameActive && activeTaskObj === this) {
                this.currentColor = '#4CAF50';
                mainButton.style.backgroundColor = this.currentColor;
            }
        }, colorDelay * 1000);
        targetClicks = 1;
        lastTarget = 1;
    }

    handleActivate() {
        if (this.currentColor === '#4CAF50') taskSuccess();
        else taskFail('failure.wrongColor');
    }

    onTimeUp() {
        taskFail('failure.colorTimeout');
    }

    cleanup() {
        clearTaskTimeouts(this);
    }

    pause() { pauseTaskTimeouts(this); }
    resume() { resumeTaskTimeouts(this); }
}

function setInstructionPreservingFinalRule(key, params = {}) {
    setInstruction(key, params);
    if (activeGlobalRule?.id !== 'finalLine') return;
    arenaInstruction.textContent = `${t('rule.decoyLine')}\n${t(key, params)}`;
    arenaInstruction.classList.add('final-authority');
}

function activateGoSignal(task, instructionKey, statusElement = null) {
    task.canActivate = true;
    setInstructionPreservingFinalRule(instructionKey);
    if (statusElement) statusElement.textContent = t(instructionKey);
    mainButton.classList.remove('waiting-signal');
    mainButton.classList.add('go-signal');
    audio.play('signal');
}

function cleanupSignalTaskPresentation() {
    mainButton.classList.remove('waiting-signal', 'go-signal', 'moving-control-0', 'moving-control-1', 'shrinking-control');
    mainButton.style.backgroundColor = '#4CAF50';
    arenaInstruction.classList.remove('mirrored-instruction', 'final-authority', 'instruction-change');
    arenaInstruction.removeAttribute('data-authority');
    decoyInstruction.style.display = 'none';
    decoyInstruction.textContent = '';
}

class TaskWait {
    setup() {
        this.canActivate = false;
        arenaInstruction.style.display = 'block';
        setInstruction('task.wait.before');
        mainButton.classList.add('waiting-signal');
        mainButton.style.backgroundColor = '#6d4c41';

        const difficultyProgress = clamp((level - 4) / 46, 0, 1);
        const centerRatio = 0.58 - (difficultyProgress * 0.2);
        const spread = 0.08 + (difficultyProgress * 0.12);
        const randomizedRatio = centerRatio + (((Math.random() * 2) - 1) * spread);
        const responseReserve = clamp(timeLeft * 0.38, 0.5, 1.1);
        this.signalDelay = clamp(timeLeft * randomizedRatio, 0.32, timeLeft - responseReserve);
        scheduleTaskTimeout(this, () => activateGoSignal(this, 'task.wait.go'), this.signalDelay * 1000);
        targetClicks = 1;
        lastTarget = 1;
    }

    handleActivate() {
        if (!this.canActivate) return taskFail('failure.waitEarly');
        taskSuccess();
    }

    getModifierTargets() { return [mainButton]; }
    getMemory() { return { previousTarget: 1, previousAnswer: 'wait' }; }
    pause() { pauseTaskTimeouts(this); }
    resume() { resumeTaskTimeouts(this); }
    onTimeUp() { taskFail('failure.waitTimeout'); }

    cleanup() {
        clearTaskTimeouts(this);
        cleanupSignalTaskPresentation();
        arenaInstruction.style.display = 'none';
    }
}

const LAST_SECOND_COLORS = [
    { key: 'color.red', css: '#c62828' },
    { key: 'color.blue', css: '#1565c0' },
    { key: 'color.green', css: '#2e7d32' },
    { key: 'color.yellow', css: '#8a7800' },
    { key: 'color.purple', css: '#6a1b9a' },
    { key: 'color.orange', css: '#b84d00' }
];

class TaskLastSecondInstruction extends GridTaskBase {
    setup() {
        const choiceCount = clamp(3 + Math.floor(level / 15), 3, LAST_SECOND_COLORS.length);
        const colors = shuffled(LAST_SECOND_COLORS).slice(0, choiceCount);
        this.initialColor = colors[0];
        this.finalColor = colors[1];
        this.finalInstructionVisible = false;
        this.setupGrid('task.lastSecond.initial', { color: t(this.initialColor.key).toLocaleUpperCase(currentLanguage) }, choiceCount > 4 ? 3 : 2);
        this.setInputLocked(true, 'task-final-instruction');

        shuffled(colors).forEach((color) => {
            const button = this.addChoice(t(color.key).toLocaleUpperCase(currentLanguage), color === this.finalColor, { className: 'color-choice' });
            button.dataset.colorKey = color.key;
            button.style.backgroundColor = color.css;
        });

        const difficultyProgress = clamp((level - 7) / 43, 0, 1);
        const responseReserve = clamp(timeLeft * 0.44, 0.58, 1.15);
        const maximumDelay = timeLeft - responseReserve;
        const centerRatio = 0.42 - (difficultyProgress * 0.14);
        const spread = 0.06 + (difficultyProgress * 0.08);
        const randomizedRatio = centerRatio + (((Math.random() * 2) - 1) * spread);
        this.changeDelay = clamp(timeLeft * randomizedRatio, 0.3, maximumDelay);

        scheduleTaskTimeout(this, () => {
            this.finalInstructionVisible = true;
            setInstructionPreservingFinalRule('task.lastSecond.final', { color: t(this.finalColor.key).toLocaleUpperCase(currentLanguage) });
            arenaInstruction.classList.add('instruction-change');
            this.grid.classList.add('final-instruction-active');
            this.setInputLocked(false, 'task-final-instruction');
            audio.play('signal');
            scheduleTaskTimeout(this, () => arenaInstruction.classList.remove('instruction-change'), 240);
        }, this.changeDelay * 1000);
    }

    localize() {
        const instructionColor = this.finalInstructionVisible ? this.finalColor : this.initialColor;
        setInstructionPreservingFinalRule(
            this.finalInstructionVisible ? 'task.lastSecond.final' : 'task.lastSecond.initial',
            { color: t(instructionColor.key).toLocaleUpperCase(currentLanguage) }
        );
        this.getModifierTargets().forEach((button) => {
            button.textContent = t(button.dataset.colorKey).toLocaleUpperCase(currentLanguage);
        });
    }

    getMemory() { return { previousAnswer: this.finalColor.key }; }
    onTimeUp() { taskFail('failure.lastSecondTimeout'); }

    cleanup() {
        cleanupSignalTaskPresentation();
        super.cleanup();
    }
}

class TaskPatienceCountdown {
    setup() {
        this.canActivate = false;
        arenaInstruction.style.display = 'block';
        sequenceContainer.style.display = 'block';
        setInstruction('task.patience.instruction');
        mainButton.classList.add('waiting-signal');
        mainButton.style.backgroundColor = '#5d4037';

        this.statusElement = document.createElement('div');
        this.statusElement.className = 'patience-countdown';
        this.statusElement.textContent = '3';
        sequenceContainer.appendChild(this.statusElement);

        const responseReserve = clamp(timeLeft * 0.32, 0.48, 0.9);
        const signalBudget = timeLeft - responseReserve;
        const maximumHesitation = Math.min(0.42, signalBudget * 0.25);
        const hasHesitation = Math.random() >= 0.5;
        this.hesitation = hasHesitation ? 0.08 + (Math.random() * Math.max(0, maximumHesitation - 0.08)) : 0;
        const countdownDuration = signalBudget - maximumHesitation;
        this.countdownStep = countdownDuration / 3;
        this.signalDelay = countdownDuration + this.hesitation;

        scheduleTaskTimeout(this, () => { this.statusElement.textContent = '2'; }, this.countdownStep * 1000);
        scheduleTaskTimeout(this, () => { this.statusElement.textContent = '1'; }, this.countdownStep * 2000);
        if (this.hesitation > 0) {
            scheduleTaskTimeout(this, () => { this.statusElement.textContent = t('task.patience.hesitation'); }, countdownDuration * 1000);
        }
        scheduleTaskTimeout(this, () => activateGoSignal(this, 'task.patience.go', this.statusElement), this.signalDelay * 1000);
        targetClicks = 1;
        lastTarget = 1;
    }

    handleActivate() {
        if (!this.canActivate) return taskFail('failure.patienceEarly');
        taskSuccess();
    }

    localize() {
        if (this.canActivate && this.statusElement) this.statusElement.textContent = t('task.patience.go');
        else if (this.statusElement?.textContent === '…') this.statusElement.textContent = t('task.patience.hesitation');
    }

    getModifierTargets() { return [mainButton]; }
    getMemory() { return { previousTarget: 1, previousAnswer: 'patience' }; }
    pause() { pauseTaskTimeouts(this); }
    resume() { resumeTaskTimeouts(this); }
    onTimeUp() { taskFail('failure.patienceTimeout'); }

    cleanup() {
        clearTaskTimeouts(this);
        cleanupSignalTaskPresentation();
        sequenceContainer.replaceChildren();
        sequenceContainer.style.display = 'none';
        arenaInstruction.style.display = 'none';
    }
}

class TaskSequence {
    setup() {
        mainButton.style.display = 'none';
        sequenceContainer.style.display = 'block';
        this.currentSequence = 1;
        this.maximumSequence = clamp(Math.floor(timeLeft / 0.48), 2, 4);

        const positions = [
            { top: '20%', left: '30%' },
            { top: '70%', left: '20%' },
            { top: '25%', left: '75%' },
            { top: '75%', left: '80%' }
        ];
        for (let index = positions.length - 1; index > 0; index -= 1) {
            const swapIndex = Math.floor(Math.random() * (index + 1));
            [positions[index], positions[swapIndex]] = [positions[swapIndex], positions[index]];
        }

        for (let number = 1; number <= this.maximumSequence; number += 1) {
            const button = document.createElement('button');
            button.type = 'button';
            button.className = 'mini-btn';
            button.textContent = String(number);
            button.setAttribute('aria-label', t('task.sequenceButton', { number }));
            button.style.top = positions[number - 1].top;
            button.style.left = positions[number - 1].left;

            button.addEventListener('click', () => {
                if (!gameActive || gameplayPaused || inputTransitionLocked || activeTaskObj !== this) return;
                if (number === this.currentSequence) {
                    button.hidden = true;
                    this.currentSequence += 1;
                    if (this.currentSequence > this.maximumSequence) taskSuccess();
                } else {
                    taskFail('failure.sequenceWrong');
                }
            });
            sequenceContainer.appendChild(button);
        }
    }

    localize() {
        sequenceContainer.querySelectorAll('.mini-btn').forEach((button) => {
            button.setAttribute('aria-label', t('task.sequenceButton', { number: button.textContent }));
        });
    }

    onTimeUp() {
        taskFail('failure.sequenceTimeout');
    }

    cleanup() {
        sequenceContainer.replaceChildren();
        sequenceContainer.style.display = 'none';
        mainButton.style.display = 'flex';
    }
}

class TaskEvade {
    setup() {
        mainButton.style.display = 'none';
        sequenceContainer.style.display = 'block';
        const maximumTarget = clamp(Math.floor(timeLeft / 0.42), 2, 4);
        const minimumTarget = Math.min(3, maximumTarget);
        this.target = minimumTarget + Math.floor(Math.random() * ((maximumTarget - minimumTarget) + 1));
        this.currentClicks = 0;

        this.evadeButton = document.createElement('button');
        this.evadeButton.type = 'button';
        this.evadeButton.className = 'mini-btn evading';
        this.localize();
        this.moveButton();

        this.evadeButton.addEventListener('click', () => {
            if (!gameActive || gameplayPaused || inputTransitionLocked || activeTaskObj !== this) return;
            this.currentClicks += 1;
            if (this.currentClicks >= this.target) {
                taskSuccess();
            } else {
                this.localize();
                this.moveButton();
            }
        });
        sequenceContainer.appendChild(this.evadeButton);
    }

    localize() {
        if (!this.evadeButton) return;
        const remaining = this.target - this.currentClicks;
        this.evadeButton.textContent = this.currentClicks === 0 ? t('task.evadeCatch') : String(remaining);
        this.evadeButton.setAttribute('aria-label', t('task.evadeButton', { remaining }));
    }

    moveButton() {
        this.evadeButton.style.top = `${15 + (Math.random() * 70)}%`;
        this.evadeButton.style.left = `${15 + (Math.random() * 70)}%`;
    }

    onTimeUp() {
        taskFail('failure.evadeTimeout');
    }

    cleanup() {
        sequenceContainer.replaceChildren();
        sequenceContainer.style.display = 'none';
        mainButton.style.display = 'flex';
    }
}

// ---------- TASK ENGINE V2: GRID, MEMORY, AND DECEPTION TASKS ----------

class TaskOddOneOut extends GridTaskBase {
    setup() {
        const band = currentDifficultyBand();
        const count = band.options;
        const pairs = level >= 25 ? [['▲', '△'], ['●', '◉'], ['■', '□']] : [['●', '◆'], ['▲', '■'], ['★', '●']];
        const [common, odd] = pairs[randomInt(0, pairs.length - 1)];
        const oddIndex = randomInt(0, count - 1);
        this.setupGrid('task.oddOneOut.instruction', {}, count > 6 ? 3 : 2);
        for (let index = 0; index < count; index += 1) this.addChoice(index === oddIndex ? odd : common, index === oddIndex);
    }
    getMemory() { return { previousAnswer: 'odd-one-out' }; }
}

class TaskNumberExtremum extends GridTaskBase {
    setup() {
        const count = clamp(currentDifficultyBand().options, 4, 7);
        const start = randomInt(level >= 13 ? -35 : 1, 28);
        const step = randomInt(1, level >= 13 ? 4 : 7);
        this.values = Array.from({ length: count }, (_, index) => start + (index * step));
        this.wantLargest = Math.random() < 0.5;
        this.answer = this.wantLargest ? Math.max(...this.values) : Math.min(...this.values);
        this.setupGrid(this.wantLargest ? 'task.extreme.largest' : 'task.extreme.smallest', {}, count > 6 ? 3 : 2);
        shuffled(this.values).forEach((value) => this.addChoice(value, value === this.answer, { fontSize: `${randomInt(16, 34)}px` }));
    }
    getMemory() { return { previousTarget: this.answer, previousAnswer: this.answer }; }
}

class TaskParity extends GridTaskBase {
    setup() {
        const count = clamp(currentDifficultyBand().options, 4, 7);
        this.wantEven = Math.random() < 0.5;
        const answer = randomInt(level >= 13 ? -15 : 1, 24) * 2 + (this.wantEven ? 0 : 1);
        const oppositeParity = this.wantEven ? 1 : 0;
        const distractorBase = randomInt(level >= 13 ? -15 : 1, 20) * 2 + oppositeParity;
        const values = [answer, ...Array.from({ length: count - 1 }, (_, index) => distractorBase + (index * 2))];
        this.answer = answer;
        this.setupGrid(this.wantEven ? 'task.parity.even' : 'task.parity.odd', {}, count > 6 ? 3 : 2);
        shuffled(values).forEach((value) => this.addChoice(value, value === answer));
    }
    getMemory() { return { previousTarget: this.answer, previousAnswer: this.answer }; }
}

const STROOP_COLORS = [
    { key: 'color.red', css: '#ff5252', symbol: '▲' },
    { key: 'color.blue', css: '#42a5f5', symbol: '●' },
    { key: 'color.green', css: '#66bb6a', symbol: '■' },
    { key: 'color.yellow', css: '#ffee58', symbol: '◆' }
];

class TaskStroop extends GridTaskBase {
    setup() {
        const mode = level >= 20 ? shuffled(['word', 'ink', 'not'])[0] : shuffled(['word', 'ink'])[0];
        const target = STROOP_COLORS[randomInt(0, STROOP_COLORS.length - 1)];
        const alternatives = shuffled(STROOP_COLORS.filter((color) => color !== target)).slice(0, 2);
        const options = [target, ...alternatives].map((color, index) => ({ ink: alternatives[index % 2] || target, word: color }));
        if (mode === 'ink') options.forEach((option, index) => { option.ink = [target, ...alternatives][index]; option.word = alternatives[(index + 1) % 2] || target; });
        if (mode === 'not') {
            options[0].ink = STROOP_COLORS.find((color) => color !== target);
            options[1].ink = target; options[2].ink = target;
        }
        this.answerColor = target.key;
        this.setupGrid(`task.stroop.${mode}`, { value: t(target.key).toLocaleUpperCase(currentLanguage) }, 3);
        shuffled(options).forEach(({ ink, word }, index) => {
            const correct = mode === 'word' ? word === target : mode === 'ink' ? ink === target : ink !== target;
            this.addChoice(`${ink.symbol} ${t(word.key).toLocaleUpperCase(currentLanguage)}`, correct, { color: ink.css, className: 'stroop-choice' });
        });
    }
    getMemory() { return { previousColor: this.answerColor, previousAnswer: this.answerColor }; }
}

class TaskPreviousMemory extends GridTaskBase {
    setup() {
        this.answer = taskMemory.previousTarget;
        const values = [this.answer, this.answer - 2, this.answer + 1, this.answer + 3];
        this.setupGrid('task.memory.instruction', {}, 2);
        shuffled(values).forEach((value) => this.addChoice(value, value === this.answer));
    }
    getMemory() { return { previousTarget: this.answer, previousAnswer: this.answer }; }
}

class TaskFlashMemory extends GridTaskBase {
    setup() {
        const length = currentDifficultyBand().memory;
        const symbols = activeGlobalRule?.id === 'emojiLiteral' ? ['🍎', '⚡', '🌙', '⭐'] : ['●', '▲', '■', '◆'];
        this.sequence = Array.from({ length }, () => symbols[randomInt(0, symbols.length - 1)]);
        this.progress = 0;
        this.setupGrid('task.flash.watch', { sequence: this.sequence.join(' ') }, 4);
        this.setInputLocked(true);
        const displayTime = clamp(timeLeft * 0.28, 0.45, 1.2);
        scheduleTaskTimeout(this, () => {
            setInstruction('task.flash.repeat');
            symbols.forEach((symbol) => this.addChoice(symbol, false));
            this.setInputLocked(false);
        }, displayTime * 1000);
    }
    onChoice(button) {
        if (button.textContent !== this.sequence[this.progress]) return taskFail('failure.wrongChoice');
        this.progress += 1;
        if (this.progress === this.sequence.length) taskSuccess();
    }
    getMemory() { return { previousAnswer: this.sequence.join('') }; }
}

class TaskAlphabetical extends GridTaskBase {
    setup() {
        const alphabets = { tr: ['A', 'Ç', 'E', 'Ğ', 'İ', 'Ö', 'Ş', 'Ü'], en: ['A', 'C', 'E', 'G', 'I', 'O', 'S', 'U'] };
        this.ordered = shuffled(alphabets[currentLanguage]).slice(0, clamp(currentDifficultyBand().memory, 3, 5));
        this.ordered.sort((a, b) => a.localeCompare(b, currentLanguage));
        this.progress = 0;
        this.setupGrid('task.alphabet.instruction', {}, 3);
        shuffled(this.ordered).forEach((letter) => this.addChoice(letter, false));
    }
    onChoice(button) {
        if (button.textContent !== this.ordered[this.progress]) return taskFail('failure.wrongChoice');
        button.disabled = true; button.classList.add('selected-choice'); this.progress += 1;
        if (this.progress === this.ordered.length) taskSuccess();
    }
    getMemory() { return { previousAnswer: this.ordered.at(-1) }; }
}

class TaskClickPattern {
    setup() {
        this.required = Math.random() < 0.5 ? 1 : 2;
        this.clicks = 0;
        this.windowMs = clamp(timeLeft * 300, 320, 440);
        setInstruction(this.required === 1 ? 'task.clickPattern.single' : 'task.clickPattern.double');
        targetClicks = this.required;
    }
    handleActivate() {
        this.clicks += 1;
        if (this.clicks > this.required) return taskFail('failure.clickSingle');
        if (this.required === 2 && this.clicks === 2) return taskSuccess();
        if (this.clicks === 1) scheduleTaskTimeout(this, () => this.required === 1 ? taskSuccess() : taskFail('failure.clickDouble'), this.windowMs);
    }
    pause() { pauseTaskTimeouts(this); }
    resume() { resumeTaskTimeouts(this); }
    onTimeUp() { taskFail(this.required === 2 ? 'failure.clickDouble' : 'failure.timeoutSlow'); }
    cleanup() { clearTaskTimeouts(this); }
    getMemory() { return { previousTarget: this.required, previousClickCount: this.required }; }
}

class TaskFakeButton extends GridTaskBase {
    setup() {
        this.setupGrid('task.fakeButton.instruction', {}, 2);
        const realIndex = randomInt(0, 3);
        for (let index = 0; index < 4; index += 1) this.addChoice(t(index === realIndex ? 'task.fakeButton.real' : 'task.fakeButton.fake'), index === realIndex, { className: index === realIndex ? 'real-target' : 'fake-target' });
    }
}

class TaskDelayedInstruction extends GridTaskBase {
    setup() {
        this.answer = randomInt(1, 4);
        this.setupGrid('task.delayed.initial', {}, 2);
        this.setInputLocked(true);
        const delay = clamp(timeLeft * 0.22, 0.35, 0.8);
        scheduleTaskTimeout(this, () => {
            setInstruction('task.delayed.final', { count: this.answer });
            for (let value = 1; value <= 4; value += 1) this.addChoice(value, value === this.answer);
            this.setInputLocked(false);
        }, delay * 1000);
    }
    getMemory() { return { previousTarget: this.answer, previousAnswer: this.answer }; }
}

class TaskPositionMemory extends GridTaskBase {
    setup() {
        this.answer = randomInt(0, 8);
        this.setupGrid('task.positionMemory.watch', {}, 3);
        for (let index = 0; index < 9; index += 1) {
            const button = this.addChoice('', index === this.answer, { ariaLabel: t('access.memoryCell', { number: index + 1 }), className: index === this.answer ? 'memory-highlight' : '' });
            button.dataset.position = String(index);
        }
        this.setInputLocked(true);
        const displayTime = clamp(timeLeft * 0.22, 0.4, 0.9);
        scheduleTaskTimeout(this, () => {
            this.getModifierTargets().forEach((button) => button.classList.remove('memory-highlight'));
            setInstruction('task.positionMemory.choose');
            this.setInputLocked(false);
        }, displayTime * 1000);
    }
    getMemory() { return { previousAnswer: this.answer }; }
}

class TaskCountSymbols extends GridTaskBase {
    setup() {
        const symbols = activeGlobalRule?.id === 'emojiLiteral' ? ['🍎', '⚡', '🌙', '⭐'] : ['★', '●', '▲', '◆'];
        this.symbol = symbols[randomInt(0, symbols.length - 1)];
        this.answer = randomInt(2, currentDifficultyBand().memory + 1);
        const total = clamp(currentDifficultyBand().options + 2, 6, 11);
        const display = Array(this.answer).fill(this.symbol);
        while (display.length < total) display.push(symbols.filter((symbol) => symbol !== this.symbol)[randomInt(0, 2)]);
        this.setupGrid('task.count.instruction', { symbol: `${this.symbol}  ${shuffled(display).join(' ')}` }, 3);
        const answers = [this.answer, Math.max(0, this.answer - 1), this.answer + 1, this.answer + 2];
        shuffled([...new Set(answers)]).forEach((value) => this.addChoice(value, value === this.answer));
    }
    getMemory() { return { previousTarget: this.answer, previousAnswer: this.answer }; }
}

class TaskYesNo extends GridTaskBase {
    setup() {
        this.number = randomInt(level >= 13 ? -12 : 1, 30);
        const claimedEven = Math.random() < 0.5;
        const statementTrue = Math.abs(this.number % 2) === (claimedEven ? 0 : 1);
        this.answerYes = activeGlobalRule?.id === 'invert' ? !statementTrue : statementTrue;
        this.setupGrid('task.yesNo.statement', { number: this.number, parity: t(claimedEven ? 'task.yesNo.even' : 'task.yesNo.odd') }, 2);
        this.addChoice(t('common.yes'), this.answerYes);
        this.addChoice(t('common.no'), !this.answerYes);
    }
    getMemory() { return { previousTarget: this.number, previousAnswer: this.answerYes }; }
}

// ---------- TASK ENGINE V2: REGISTRY, SELECTION, MODIFIERS, AND RULES ----------

const ALL_GRID_MODIFIERS = ['mirrored', 'delayed', 'moving', 'shrinking', 'decoy', 'swap'];
const TASK_REGISTRY = [
    { id: 'standard', category: 'reaction', minLevel: 1, weight: 7, difficulty: 1, minDuration: 1.0, nameKey: 'task.standard.name', inputs: ['pointer', 'keyboard'], rules: [], modifiers: [], create: () => { const target = randomInt(1, 4); return new TaskStandard({ textKey: 'task.quickPress', textParams: { count: target }, target }); } },
    { id: 'package', category: 'deception', minLevel: 4, weight: 3, difficulty: 3, minDuration: 1.3, nameKey: 'task.package.name', inputs: ['pointer', 'keyboard'], rules: [], modifiers: [], create: () => { startRandomPackage(); return new TaskStandard(packageQueue.shift()); } },
    { id: 'hold', category: 'timing', minLevel: 3, weight: 3, difficulty: 2, minDuration: 1.2, nameKey: 'task.hold.name', inputs: ['pointer', 'keyboard'], rules: [], modifiers: [], create: () => new TaskHoldRelease() },
    { id: 'colorShift', category: 'reaction', minLevel: 3, weight: 3, difficulty: 2, minDuration: 1.2, nameKey: 'task.colorShift.name', inputs: ['pointer', 'keyboard'], rules: [], modifiers: [], create: () => new TaskColorShift() },
    { id: 'wait', category: 'inhibition', minLevel: 5, weight: 3.5, difficulty: 2, minDuration: 1.2, nameKey: 'task.wait.name', inputs: ['pointer', 'keyboard'], rules: ['finalLine'], modifiers: ['mirrored', 'moving', 'shrinking', 'decoy'], create: () => new TaskWait() },
    { id: 'lastSecondInstruction', category: 'inhibition', minLevel: 7, weight: 3, difficulty: 3, minDuration: 1.5, nameKey: 'task.lastSecond.name', inputs: ['pointer', 'keyboard'], rules: ['finalLine', 'oddWait'], modifiers: ALL_GRID_MODIFIERS, create: () => new TaskLastSecondInstruction() },
    { id: 'patienceCountdown', category: 'timing', minLevel: 9, weight: 3, difficulty: 3, minDuration: 1.5, nameKey: 'task.patience.name', inputs: ['pointer', 'keyboard'], rules: ['finalLine'], modifiers: ['mirrored', 'moving', 'shrinking', 'decoy'], create: () => new TaskPatienceCountdown() },
    { id: 'sequence', category: 'sequence', minLevel: 4, weight: 3, difficulty: 2, minDuration: 1.2, nameKey: 'task.sequence.name', inputs: ['pointer', 'keyboard'], rules: [], modifiers: [], create: () => new TaskSequence() },
    { id: 'evade', category: 'precision', minLevel: 6, weight: 2.5, difficulty: 3, minDuration: 1.4, nameKey: 'task.evade.name', inputs: ['pointer', 'keyboard'], rules: [], modifiers: [], create: () => new TaskEvade() },
    { id: 'oddOneOut', category: 'visual', minLevel: 2, weight: 4, difficulty: 2, minDuration: 1.2, nameKey: 'task.oddOneOut.name', inputs: ['pointer', 'keyboard'], rules: ['ignoreRed', 'finalLine', 'oddWait'], modifiers: ALL_GRID_MODIFIERS, create: () => new TaskOddOneOut() },
    { id: 'numberExtremum', category: 'arithmetic', minLevel: 3, weight: 4, difficulty: 2, minDuration: 1.2, nameKey: 'task.extreme.name', inputs: ['pointer', 'keyboard'], rules: ['ignoreRed', 'finalLine', 'oddWait'], modifiers: ALL_GRID_MODIFIERS, create: () => new TaskNumberExtremum() },
    { id: 'parity', category: 'arithmetic', minLevel: 5, weight: 4, difficulty: 2, minDuration: 1.2, nameKey: 'task.parity.name', inputs: ['pointer', 'keyboard'], rules: ['ignoreRed', 'finalLine', 'oddWait'], modifiers: ALL_GRID_MODIFIERS, create: () => new TaskParity() },
    { id: 'stroop', category: 'visual', minLevel: 7, weight: 3.5, difficulty: 3, minDuration: 1.4, nameKey: 'task.stroop.name', inputs: ['pointer', 'keyboard'], rules: ['ignoreRed', 'finalLine', 'oddWait'], modifiers: ['mirrored', 'delayed', 'decoy', 'swap'], create: () => new TaskStroop() },
    { id: 'previousMemory', category: 'memory', minLevel: 8, weight: 3, difficulty: 3, minDuration: 1.2, nameKey: 'task.memory.name', inputs: ['pointer', 'keyboard'], rules: ['ignoreRed', 'finalLine'], modifiers: ['mirrored', 'moving', 'shrinking', 'decoy', 'swap'], eligible: () => Number.isFinite(taskMemory.previousTarget), create: () => new TaskPreviousMemory() },
    { id: 'flashMemory', category: 'memory', minLevel: 9, weight: 3, difficulty: 4, minDuration: 1.6, nameKey: 'task.flash.name', inputs: ['pointer', 'keyboard'], rules: ['finalLine', 'emojiLiteral'], modifiers: ['mirrored', 'shrinking', 'decoy'], create: () => new TaskFlashMemory() },
    { id: 'alphabetical', category: 'language', minLevel: 6, weight: 3.5, difficulty: 3, minDuration: 1.3, nameKey: 'task.alphabet.name', inputs: ['pointer', 'keyboard'], rules: ['ignoreRed', 'finalLine', 'oddWait'], modifiers: ALL_GRID_MODIFIERS, create: () => new TaskAlphabetical() },
    { id: 'clickPattern', category: 'deception', minLevel: 8, weight: 3, difficulty: 3, minDuration: 1.1, nameKey: 'task.clickPattern.name', inputs: ['pointer', 'keyboard'], rules: [], modifiers: [], create: () => new TaskClickPattern() },
    { id: 'fakeButton', category: 'deception', minLevel: 7, weight: 3, difficulty: 3, minDuration: 1.2, nameKey: 'task.fakeButton.name', inputs: ['pointer', 'keyboard'], rules: ['ignoreRed', 'finalLine', 'oddWait'], modifiers: ['mirrored', 'delayed', 'moving', 'shrinking', 'decoy', 'swap'], create: () => new TaskFakeButton() },
    { id: 'delayedInstruction', category: 'inhibition', minLevel: 10, weight: 3, difficulty: 3, minDuration: 1.5, nameKey: 'task.delayed.name', inputs: ['pointer', 'keyboard'], rules: ['finalLine'], modifiers: ['mirrored', 'shrinking', 'decoy'], create: () => new TaskDelayedInstruction() },
    { id: 'positionMemory', category: 'memory', minLevel: 11, weight: 3, difficulty: 4, minDuration: 1.5, nameKey: 'task.positionMemory.name', inputs: ['pointer', 'keyboard'], rules: ['finalLine'], modifiers: ['mirrored', 'decoy'], create: () => new TaskPositionMemory() },
    { id: 'countSymbols', category: 'visual', minLevel: 5, weight: 4, difficulty: 2, minDuration: 1.3, nameKey: 'task.count.name', inputs: ['pointer', 'keyboard'], rules: ['ignoreRed', 'finalLine', 'oddWait', 'emojiLiteral'], modifiers: ALL_GRID_MODIFIERS, create: () => new TaskCountSymbols() },
    { id: 'yesNo', category: 'language', minLevel: 8, weight: 3, difficulty: 3, minDuration: 1.2, nameKey: 'task.yesNo.name', inputs: ['pointer', 'keyboard'], rules: ['invert', 'ignoreRed', 'finalLine', 'oddWait'], modifiers: ALL_GRID_MODIFIERS, create: () => new TaskYesNo() }
];

const MODIFIER_DEFINITIONS = {
    mirrored: { minLevel: 25, nameKey: 'modifier.mirrored' },
    delayed: { minLevel: 27, nameKey: 'modifier.delayed' },
    moving: { minLevel: 29, nameKey: 'modifier.moving' },
    shrinking: { minLevel: 31, nameKey: 'modifier.shrinking' },
    decoy: { minLevel: 34, nameKey: 'modifier.decoy' },
    swap: { minLevel: 37, nameKey: 'modifier.swap' }
};

const GLOBAL_RULES = [
    { id: 'invert', minLevel: 13, nameKey: 'rule.invert' },
    { id: 'ignoreRed', minLevel: 15, nameKey: 'rule.ignoreRed' },
    { id: 'finalLine', minLevel: 17, nameKey: 'rule.finalLine' },
    { id: 'oddWait', minLevel: 19, nameKey: 'rule.oddWait' },
    { id: 'emojiLiteral', minLevel: 21, nameKey: 'rule.emojiLiteral' }
];

function weightedChoice(items) {
    const total = items.reduce((sum, item) => sum + item.selectionWeight, 0);
    let cursor = Math.random() * total;
    for (const item of items) {
        cursor -= item.selectionWeight;
        if (cursor <= 0) return item;
    }
    return items.at(-1);
}

function getTaskCandidateWeights() {
    return TASK_REGISTRY.filter((definition) => (
        level >= definition.minLevel &&
        timeLeft >= definition.minDuration &&
        definition.id !== taskHistory.at(-1) &&
        (!definition.eligible || definition.eligible()) &&
        (!activeGlobalRule || definition.rules.includes(activeGlobalRule.id))
    )).map((definition) => {
        let selectionWeight = definition.weight;
        const recentIndex = [...taskHistory].reverse().indexOf(definition.id);
        if (recentIndex >= 0) selectionWeight *= (definition.recentRepeatPenalty ?? 0.18) + (recentIndex * 0.12);
        if (categoryHistory.at(-1) === definition.category) selectionWeight *= 0.3;
        if (categoryHistory.slice(-3).filter((category) => category === definition.category).length >= 2) selectionWeight *= 0.45;
        return { ...definition, selectionWeight: Math.max(0.05, selectionWeight) };
    });
}

function selectTaskDefinition() {
    const candidates = getTaskCandidateWeights();
    const fallback = TASK_REGISTRY.find((definition) => definition.id === 'standard');
    const selected = candidates.length ? weightedChoice(candidates) : { ...fallback, selectionWeight: fallback.weight };
    return selected;
}

function rememberSelection(definition) {
    taskHistory.push(definition.id); categoryHistory.push(definition.category);
    taskHistory = taskHistory.slice(-6); categoryHistory = categoryHistory.slice(-5);
}

function selectModifiers(definition) {
    const band = currentDifficultyBand();
    if (!band.modifiers || Math.random() > (band.modifiers === 1 ? 0.55 : 0.78)) return [];
    const available = shuffled(definition.modifiers.filter((id) => level >= MODIFIER_DEFINITIONS[id].minLevel));
    const selected = [];
    for (const id of available) {
        if (selected.length >= band.modifiers) break;
        if ((id === 'moving' && selected.includes('swap')) || (id === 'swap' && selected.includes('moving'))) continue;
        selected.push(id);
    }
    return selected;
}

function applyModifiers(task, modifiers) {
    modifiers.forEach((id) => {
        const targets = typeof task.getModifierTargets === 'function' ? task.getModifierTargets() : [];
        if (id === 'mirrored') arenaInstruction.classList.add('mirrored-instruction');
        if (id === 'moving') targets.forEach((target, index) => target.classList.add(`moving-control-${index % 2}`));
        if (id === 'shrinking') targets.forEach((target) => target.classList.add('shrinking-control'));
        if (id === 'decoy') { decoyInstruction.textContent = t('modifier.decoyText'); decoyInstruction.style.display = 'block'; arenaInstruction.dataset.authority = t('modifier.realText'); }
        if (id === 'delayed' && typeof task.setInputLocked === 'function') {
            task.setInputLocked(true, 'modifier-delayed'); showAnnouncement('modifier.wait');
            scheduleTaskTimeout(task, () => { task.setInputLocked(false, 'modifier-delayed'); showAnnouncement('modifier.ready'); }, clamp(timeLeft * 180, 280, 600));
        }
        if (id === 'swap' && task.grid) {
            task.setInputLocked(true, 'modifier-swap');
            scheduleTaskTimeout(task, () => {
                shuffled([...task.grid.children]).forEach((child) => task.grid.appendChild(child));
                task.grid.classList.add('position-swapped'); task.setInputLocked(false, 'modifier-swap');
            }, clamp(timeLeft * 160, 260, 520));
        }
    });
}

function prepareGlobalRule() {
    const band = currentDifficultyBand();
    if (activeGlobalRule || completedTaskCount < 10 || Math.random() > band.globalChance) return false;
    const possible = GLOBAL_RULES.filter((rule) => level >= rule.minLevel && TASK_REGISTRY.some((task) => task.rules.includes(rule.id) && level >= task.minLevel && timeLeft >= task.minDuration));
    if (!possible.length) return false;
    const rule = possible[randomInt(0, possible.length - 1)];
    activeGlobalRule = { ...rule, remaining: randomInt(2, level >= 40 ? 4 : 3) };
    return true;
}

function applyGlobalRuleToTask(task) {
    if (!activeGlobalRule) return;
    if (activeGlobalRule.id === 'ignoreRed' && typeof task.markRedDecoy === 'function') task.markRedDecoy();
    if (activeGlobalRule.id === 'finalLine') {
        arenaInstruction.textContent = `${t('rule.decoyLine')}\n${t(currentInstruction.key, currentInstruction.params)}`;
        arenaInstruction.classList.add('final-authority');
    }
    if (activeGlobalRule.id === 'oddWait' && level % 2 === 1 && typeof task.setInputLocked === 'function') {
        task.setInputLocked(true, 'rule-odd'); showAnnouncement('modifier.wait');
        scheduleTaskTimeout(task, () => { task.setInputLocked(false, 'rule-odd'); showAnnouncement('modifier.ready'); }, clamp(timeLeft * 180, 280, 600));
    }
}

function renderGameplayIndicators() {
    globalRuleBadge.textContent = activeGlobalRule ? t('rule.label', { remaining: activeGlobalRule.remaining, name: t(activeGlobalRule.nameKey) }) : '';
    modifierIndicator.textContent = activeModifiers.length ? t('modifier.label', { name: activeModifiers.map((id) => t(MODIFIER_DEFINITIONS[id].nameKey)).join(' + ') }) : '';
}

function showAnnouncement(key, params = {}, duration = 850) {
    window.clearTimeout(announcementTimer);
    announcement.textContent = t(key, params); announcement.classList.add('visible');
    announcementTimer = window.setTimeout(() => announcement.classList.remove('visible'), duration);
}

function storeDiscovery(id) {
    if (storedDiscoveries.includes(id)) return;
    storedDiscoveries.push(id);
    safeStorageSet(STORAGE_KEYS.discoveries, JSON.stringify(storedDiscoveries));
}

function announceNewMechanic(definition) {
    if (!definition?.nameKey || seenMechanics.has(definition.id)) return 140;
    seenMechanics.add(definition.id);
    storeDiscovery(`task:${definition.id}`);
    showAnnouncement('feedback.newMechanic', { name: t(definition.nameKey) }, 1100);
    return 1150;
}

function flashFeedback(type) {
    document.body.classList.remove('success-flash');
    if (type === 'success') { void document.body.offsetWidth; document.body.classList.add('success-flash'); window.setTimeout(() => document.body.classList.remove('success-flash'), 220); }
}

let activePointerId = null;
let pointerTask = null;
let keyboardTask = null;

function resetPointerInput() {
    if (activePointerId !== null && mainButton.hasPointerCapture?.(activePointerId)) {
        try {
            mainButton.releasePointerCapture(activePointerId);
        } catch (error) {
            // The browser may already have released capture during cancellation.
        }
    }
    activePointerId = null;
    pointerTask = null;
}

mainButton.addEventListener('pointerdown', (event) => {
    if (!gameActive || gameplayPaused || inputTransitionLocked || !activeTaskObj || event.isPrimary === false) return;
    if (event.pointerType === 'mouse' && event.button !== 0) return;
    event.preventDefault();
    if (activePointerId !== null) return;

    activePointerId = event.pointerId;
    pointerTask = activeTaskObj;
    try {
        mainButton.setPointerCapture(event.pointerId);
    } catch (error) {
        // Pointer capture is an enhancement; pointerup still works on the button.
    }

    if (typeof pointerTask.handlePressStart === 'function') {
        pointerTask.handlePressStart();
    }
});

mainButton.addEventListener('pointerup', (event) => {
    if (event.pointerId !== activePointerId) return;
    event.preventDefault();
    const completedTask = pointerTask;
    resetPointerInput();

    if (!gameActive || gameplayPaused || inputTransitionLocked || activeTaskObj !== completedTask) return;
    if (typeof completedTask.handlePressEnd === 'function') completedTask.handlePressEnd();
    else if (typeof completedTask.handleActivate === 'function') completedTask.handleActivate();
});

mainButton.addEventListener('pointercancel', (event) => {
    if (event.pointerId !== activePointerId) return;
    const cancelledTask = pointerTask;
    resetPointerInput();
    if (gameActive && activeTaskObj === cancelledTask && typeof cancelledTask.handlePressCancel === 'function') {
        cancelledTask.handlePressCancel();
    }
});

mainButton.addEventListener('click', (event) => {
    event.preventDefault();
});

mainButton.addEventListener('keydown', (event) => {
    if (!gameActive || gameplayPaused || inputTransitionLocked || !activeTaskObj || !['Enter', ' '].includes(event.key)) return;
    event.preventDefault();
    if (event.repeat) return;

    const task = activeTaskObj;
    if (typeof task.handlePressStart === 'function') {
        keyboardTask = task;
        task.handlePressStart();
    } else if (typeof task.handleActivate === 'function') {
        keyboardTask = null;
        task.handleActivate();
    }
});

mainButton.addEventListener('keyup', (event) => {
    if (!['Enter', ' '].includes(event.key) || !keyboardTask) return;
    event.preventDefault();
    const completedTask = keyboardTask;
    keyboardTask = null;
    if (gameActive && activeTaskObj === completedTask && typeof completedTask.handlePressEnd === 'function') {
        completedTask.handlePressEnd();
    }
});

function gameOver(failureKey, failureParams = {}) {
    gameActive = false;
    clearTimer();
    window.clearTimeout(levelTransitionTimer);
    levelTransitionTimer = null;
    resetInputState();
    failedTaskSnapshot = {
        id: activeTaskDefinition?.id || 'legacy',
        nameKey: activeTaskDefinition?.nameKey || null,
        category: activeTaskDefinition?.category || 'reaction',
        modifiers: [...activeModifiers],
        ruleNameKey: activeGlobalRule?.nameKey || null
    };
    if (activeTaskObj && typeof activeTaskObj.cleanup === 'function') {
        activeTaskObj.cleanup();
    }
    activeTaskObj = null;
    combo = 0;
    updatePersonalBest(level);
    document.body.classList.remove('timer-urgent', 'timer-critical', 'task-transition', 'cursor-decoy', 'game-paused');
    const totalSeconds = Math.floor((performance.now() - startTime - totalPausedMs) / 1000);
    finalResult = {
        failureKey,
        failureParams,
        nickname,
        level,
        score: sessionScore,
        seconds: totalSeconds,
        highestCombo,
        newBest: sessionPersonalBest,
        sarcasm: randomInt(1, 5)
    };
    renderDynamicTranslations();
    gameOverScreen.classList.add('active');
    gameOverScreen.querySelector('.result-panel')?.focus();
}

function cleanSessionRuntime() {
    gameActive = false;
    gameplayPaused = false;
    pauseReason = null;
    clearTimer();
    window.clearTimeout(levelTransitionTimer);
    window.clearTimeout(menuStartTimer);
    window.clearTimeout(announcementTimer);
    levelTransitionTimer = null;
    menuStartTimer = null;
    document.querySelectorAll('.mode-btn').forEach((modeButton) => { modeButton.disabled = false; });
    inputTransitionLocked = false;
    resetInputState();
    if (activeTaskObj && typeof activeTaskObj.cleanup === 'function') activeTaskObj.cleanup();
    activeTaskObj = null;
    activeTaskDefinition = null;
    activeModifiers = [];
    activeGlobalRule = null;
    packageQueue = [];
    taskHistory = [];
    categoryHistory = [];
    taskMemory = {};
    audio.stopAll();
    sequenceContainer.replaceChildren();
    announcement.classList.remove('visible');
    gameOverScreen.classList.remove('active');
    pauseScreen.classList.remove('active');
    document.body.classList.remove('timer-urgent', 'timer-critical', 'task-transition', 'cursor-decoy', 'game-paused', 'success-flash');
}

document.getElementById('retry-button').addEventListener('click', () => {
    cleanSessionRuntime();
    stage.classList.add('is-playing');
    startGame();
});

function returnToMenu() {
    cleanSessionRuntime();
    stage.classList.remove('is-playing');
    finalResult = null;
    renderDynamicTranslations();
    document.querySelector?.('.mode-btn')?.focus();
}

document.getElementById('menu-button').addEventListener('click', returnToMenu);
document.getElementById('pause-menu-button').addEventListener('click', returnToMenu);

function pauseGame(reason = 'manual') {
    if (!gameActive || gameplayPaused) return false;
    gameplayPaused = true;
    pauseReason = reason;
    pausedStartedAt = performance.now();
    resetInputState();
    audio.stopAll();
    if (inputTransitionLocked) {
        levelTransitionRemaining = Math.max(0, levelTransitionDeadline - performance.now());
        window.clearTimeout(levelTransitionTimer);
        levelTransitionTimer = null;
    } else {
        timeLeft = Math.max(0, (timerDeadline - performance.now()) / 1000);
        clearTimer();
        if (activeTaskObj && typeof activeTaskObj.pause === 'function') activeTaskObj.pause();
    }
    document.body.classList.add('game-paused');
    pauseScreen.classList.add('active');
    pauseScreen.querySelector('.terminal-dialog')?.focus();
    return true;
}

function resumeGame() {
    if (!gameActive || !gameplayPaused || document.hidden) return false;
    totalPausedMs += performance.now() - pausedStartedAt;
    gameplayPaused = false;
    pauseReason = null;
    pauseScreen.classList.remove('active');
    document.body.classList.remove('game-paused');
    resetInputState();
    if (inputTransitionLocked) {
        armTaskTransition(Math.max(100, levelTransitionRemaining));
    } else {
        inputTransitionLocked = true;
        window.setTimeout(() => {
            if (!gameActive || gameplayPaused) return;
            if (activeTaskObj && typeof activeTaskObj.resume === 'function') activeTaskObj.resume();
            inputTransitionLocked = false;
            startTimer();
        }, 100);
    }
    mainButton.focus();
    return true;
}

pauseButton.addEventListener('click', () => pauseGame('manual'));
document.getElementById('resume-button').addEventListener('click', resumeGame);

document.querySelectorAll('.sound-toggle').forEach((button) => {
    button.addEventListener('click', () => { audio.unlock(); audio.setEnabled(!soundEnabled); audio.play('menu'); });
});

document.addEventListener('pointerdown', () => audio.unlock(), { once: true });
document.addEventListener('keydown', () => audio.unlock(), { once: true });

document.getElementById('how-to-button').addEventListener('click', () => {
    audio.play('menu'); document.getElementById('how-to-panel').hidden = false; document.getElementById('how-to-close').focus();
});
document.getElementById('how-to-close').addEventListener('click', () => {
    document.getElementById('how-to-panel').hidden = true; document.getElementById('how-to-button').focus();
});
document.getElementById('dismiss-tip').addEventListener('click', () => {
    tipSeen = true; safeStorageSet(STORAGE_KEYS.tipSeen, true); firstRunTip.classList.remove('active'); document.querySelector?.('.mode-btn')?.focus();
});

document.addEventListener('keydown', (event) => {
    if (!gameActive) return;
    if ((event.key === 'p' || event.key === 'P' || event.key === 'Escape') && !gameplayPaused) { event.preventDefault(); pauseGame('manual'); }
    else if ((event.key === 'p' || event.key === 'P' || event.key === 'Escape') && gameplayPaused && !document.hidden) { event.preventDefault(); resumeGame(); }
});

document.addEventListener('visibilitychange', () => {
    if (!gameActive) return;
    if (document.hidden) pauseGame('visibility');
});

document.getElementById('game-container').addEventListener('contextmenu', (event) => {
    if (gameActive) event.preventDefault();
});

if (!tipSeen) {
    firstRunTip.classList.add('active');
    firstRunTip.querySelector('.terminal-dialog')?.focus();
}

if (typeof navigator !== 'undefined' && 'serviceWorker' in navigator && window.location.protocol !== 'file:') {
    window.addEventListener('load', () => navigator.serviceWorker.register('./sw.js').catch(() => {}));
}

applyTranslations();
