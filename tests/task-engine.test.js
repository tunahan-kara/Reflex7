const assert = require('assert');
const fs = require('fs');
const path = require('path');
const vm = require('vm');

class ClassList {
    constructor() { this.values = new Set(); }
    add(...names) { names.forEach((name) => this.values.add(name)); }
    remove(...names) { names.forEach((name) => this.values.delete(name)); }
    toggle(name, force) {
        const enabled = force === undefined ? !this.values.has(name) : force;
        enabled ? this.values.add(name) : this.values.delete(name);
        return enabled;
    }
    contains(name) { return this.values.has(name); }
}

class FakeElement {
    constructor(id = '', dataset = {}) {
        this.id = id;
        this.dataset = dataset;
        this.textContent = '';
        this.value = '';
        this.hidden = false;
        this.disabled = false;
        this.className = '';
        this.classList = new ClassList();
        this.children = [];
        this.listeners = {};
        this.attributes = {};
        this.capture = new Set();
        this.style = { setProperty: (name, value) => { this.style[name] = String(value); } };
    }
    addEventListener(type, listener) { (this.listeners[type] ||= []).push(listener); }
    dispatch(type, properties = {}) {
        const event = {
            type, preventDefault() { this.defaultPrevented = true; }, isPrimary: true,
            button: 0, pointerId: 1, pointerType: 'mouse', repeat: false, ...properties
        };
        (this.listeners[type] || []).forEach((listener) => listener(event));
        return event;
    }
    setAttribute(name, value) { this.attributes[name] = String(value); }
    removeAttribute(name) { delete this.attributes[name]; }
    appendChild(child) { this.children = this.children.filter((item) => item !== child); this.children.push(child); return child; }
    replaceChildren(...children) { this.children = children; }
    querySelector(selector) {
        const className = selector.startsWith('.') ? selector.slice(1) : '';
        return this.children.find((child) => child.className.split(' ').includes(className)) || null;
    }
    querySelectorAll(selector) {
        const className = selector.startsWith('.') ? selector.slice(1) : '';
        return this.children.filter((child) => child.className.split(' ').includes(className));
    }
    setPointerCapture(id) { this.capture.add(id); }
    releasePointerCapture(id) { this.capture.delete(id); }
    hasPointerCapture(id) { return this.capture.has(id); }
    focus() { this.focused = true; }
    get offsetWidth() { return 100; }
}

class FakeAudioContext {
    constructor() { this.currentTime = 0; this.destination = {}; this.oscillators = []; }
    resume() {}
    createGain() {
        return { gain: { setValueAtTime() {}, exponentialRampToValueAtTime() {} }, connect() {} };
    }
    createOscillator() {
        const oscillator = {
            frequency: { setValueAtTime() {} }, connect() {}, start() { this.started = true; },
            stop() { this.stopped = true; }, onended: null
        };
        this.oscillators.push(oscillator);
        return oscillator;
    }
}

const elementIds = [
    'game-container', 'stage', 'nickname-input', 'main-button', 'instruction', 'arena-instruction',
    'decoy-instruction', 'announcement', 'gameplay-indicators', 'global-rule-badge', 'modifier-indicator',
    'level-display', 'high-score-display', 'timer-bar', 'sequence-container',
    'game-over-screen', 'pause-screen', 'first-run-tip', 'fail-message', 'result-player-stat', 'final-level-stat',
    'final-score-stat', 'best-level-stat', 'best-score-stat', 'total-time-stat', 'highest-combo-stat',
    'failed-task-stat', 'failed-context-stat', 'new-best-result', 'sarcasm-message', 'score-display',
    'combo-display', 'pause-button', 'retry-button', 'menu-button', 'pause-menu-button', 'resume-button',
    'how-to-button', 'how-to-close', 'how-to-panel', 'dismiss-tip'
];

function createEnvironment(options = {}) {
    const storage = options.storage || new Map();
    let now = options.now || 1000;
    const elements = Object.fromEntries(elementIds.map((id) => [id, new FakeElement(id)]));
    const terminalDialog = new FakeElement(); terminalDialog.className = 'terminal-dialog';
    const resultPanel = new FakeElement(); resultPanel.className = 'result-panel';
    elements['pause-screen'].appendChild(terminalDialog);
    elements['first-run-tip'].appendChild(terminalDialog);
    elements['game-over-screen'].appendChild(resultPanel);

    const languageButtons = ['tr', 'en', 'tr', 'en'].map((language) => new FakeElement('', {
        language, i18nAriaLabel: `menu.${language === 'tr' ? 'turkish' : 'english'}`
    }));
    languageButtons.forEach((button) => { button.className = 'language-btn'; });
    const modeButtons = [new FakeElement('', { time: '7.0', i18n: 'menu.slowMode' }), new FakeElement('', { time: '4.0', i18n: 'menu.fastMode' })];
    modeButtons.forEach((button) => { button.className = 'mode-btn'; });
    const soundButtons = [new FakeElement(), new FakeElement()];
    soundButtons.forEach((button) => { button.className = 'sound-toggle'; button.dataset.i18nAriaLabel = 'audio.toggle'; });
    const translated = [...modeButtons, elements['retry-button'], elements['menu-button'], elements['resume-button']];
    elements['nickname-input'].dataset.i18nPlaceholder = 'menu.nicknamePlaceholder';
    elements['retry-button'].dataset.i18n = 'result.retry';
    elements['menu-button'].dataset.i18n = 'result.menu';
    elements['resume-button'].dataset.i18n = 'pause.resume';
    elements['pause-button'].dataset.i18nAriaLabel = 'pause.button';

    const body = new FakeElement('body');
    const document = {
        body, hidden: false, documentElement: new FakeElement('html'), listeners: {}, title: '',
        getElementById: (id) => elements[id] || null,
        createElement: () => new FakeElement(),
        querySelector(selector) {
            if (selector === '.mode-btn') return modeButtons[0];
            return null;
        },
        querySelectorAll(selector) {
            if (selector === '.language-btn') return languageButtons;
            if (selector === '.mode-btn') return modeButtons;
            if (selector === '.sound-toggle') return soundButtons;
            if (selector === '[data-i18n]') return translated;
            if (selector === '[data-i18n-placeholder]') return [elements['nickname-input']];
            if (selector === '[data-i18n-aria-label]') return [...languageButtons, ...soundButtons, elements['pause-button']];
            return [];
        },
        addEventListener(type, listener) { (this.listeners[type] ||= []).push(listener); },
        dispatch(type, properties = {}) {
            const event = { type, preventDefault() { this.defaultPrevented = true; }, ...properties };
            (this.listeners[type] || []).forEach((listener) => listener(event));
        }
    };

    let timerId = 0;
    const timers = new Map();
    const addTimer = (callback, delay = 0, interval = false) => {
        const id = ++timerId; timers.set(id, { callback, delay, interval }); return id;
    };
    const storageApi = {
        getItem(key) { if (options.blockStorage) throw new Error('storage denied'); return storage.has(key) ? storage.get(key) : null; },
        setItem(key, value) { if (options.blockStorage) throw new Error('storage denied'); storage.set(key, String(value)); }
    };
    const window = {
        localStorage: storageApi, AudioContext: options.audio === false ? undefined : FakeAudioContext,
        setTimeout: (callback, delay) => addTimer(callback, delay), clearTimeout: (id) => timers.delete(id),
        setInterval: (callback, delay) => addTimer(callback, delay, true), clearInterval: (id) => timers.delete(id),
        location: { protocol: 'file:' }, addEventListener() {}
    };
    const math = Object.create(Math);
    const performance = { now: () => now };
    const context = vm.createContext({ console, document, window, performance, Math: math, Object, Number, String, JSON });
    const source = fs.readFileSync('script.js', 'utf8');
    const exports = `
        globalThis.reflex7Test = {
            language: () => currentLanguage,
            keys: () => ({ tr: Object.keys(translations.tr), en: Object.keys(translations.en) }),
            registry: () => TASK_REGISTRY,
            records: () => JSON.parse(JSON.stringify(modeRecords)),
            state: () => ({ level, sessionScore, combo, highestCombo, gameActive, gameplayPaused, selectedMode, activeModifiers: [...activeModifiers], activeGlobalRule, taskHistory: [...taskHistory], transitionLocked: inputTransitionLocked, soundEnabled, tipSeen, discoveries: [...storedDiscoveries] }),
            setState: (state) => {
                level = state.level ?? level; timeLeft = state.duration ?? timeLeft; timerDuration = state.timerDuration ?? timerDuration;
                taskHistory = state.tasks ? [...state.tasks] : []; categoryHistory = state.categories ? [...state.categories] : [];
                taskMemory = state.memory ? { ...state.memory } : {}; activeGlobalRule = state.rule === undefined ? activeGlobalRule : state.rule;
            },
            weights: () => getTaskCandidateWeights().map(({ id, category, selectionWeight, rules, modifiers }) => ({ id, category, selectionWeight, rules, modifiers })),
            selectMany: (count, seed) => {
                let value = seed >>> 0; Math.random = () => ((value = ((value * 1664525) + 1013904223) >>> 0) / 4294967296);
                taskHistory = []; categoryHistory = []; activeGlobalRule = null; taskMemory = { previousTarget: 4 }; level = 50; timeLeft = 2;
                const selected = [];
                for (let index = 0; index < count; index += 1) { const task = selectTaskDefinition(); selected.push(task.id); rememberSelection(task); }
                return selected;
            },
            modifiersFor: (id, seed) => {
                let value = seed >>> 0; Math.random = () => ((value = ((value * 1103515245) + 12345) >>> 0) / 4294967296);
                level = 50; return selectModifiers(TASK_REGISTRY.find((task) => task.id === id));
            },
            instantiate: (id, randomValue = 0.4, overrides = {}) => {
                const definition = TASK_REGISTRY.find((task) => task.id === id); Math.random = () => randomValue;
                level = overrides.level ?? Math.max(1, definition.minLevel); timeLeft = overrides.duration ?? definition.minDuration; taskMemory = { previousTarget: 4 };
                gameActive = true; gameplayPaused = false; inputTransitionLocked = false; activeGlobalRule = null;
                const task = definition.create(); activeTaskObj = task; task.setup();
                const timeoutMaximum = task._trackedTimeouts ? Math.max(0, ...[...task._trackedTimeouts].map((record) => record.remaining)) : 0;
                const choices = typeof task.getModifierTargets === 'function' ? task.getModifierTargets() : [];
                const result = { task, definition, timeoutMaximum, choiceCount: choices.length, correctCount: choices.filter((choice) => choice.dataset.correct === 'true').length };
                task.cleanup(); gameActive = false; activeTaskObj = null; return result;
            },
            gateScenario: (id, randomValue = 0.4) => {
                const definition = TASK_REGISTRY.find((task) => task.id === id); Math.random = () => randomValue;
                const originalSuccess = taskSuccess; const originalFail = taskFail; let outcome = null;
                taskSuccess = () => { outcome = { type: 'success' }; };
                taskFail = (key) => { outcome = { type: 'failure', key }; };
                level = Math.max(10, definition.minLevel); timeLeft = Math.max(2, definition.minDuration); activeGlobalRule = null;
                gameActive = true; gameplayPaused = false; inputTransitionLocked = false;
                const task = definition.create(); activeTaskObj = task; task.setup();
                const earlyTarget = typeof task.getModifierTargets === 'function' ? task.getModifierTargets()[0] : null;
                if (earlyTarget && id === 'lastSecondInstruction') earlyTarget.dispatch('click');
                else {
                    mainButton.dispatch('pointerdown', { pointerId: 31, pointerType: 'touch' });
                    mainButton.dispatch('pointerup', { pointerId: 31, pointerType: 'touch' });
                }
                const earlyOutcome = outcome; outcome = null;
                const signalRecord = [...(task._trackedTimeouts || [])].sort((a, b) => b.remaining - a.remaining)[0];
                signalRecord.callback();
                const unlocked = id === 'lastSecondInstruction' ? !task.inputLocked : task.canActivate;
                if (id === 'lastSecondInstruction') task.getModifierTargets().find((button) => button.dataset.correct === 'true').dispatch('click');
                else {
                    mainButton.dispatch('keydown', { key: 'Enter' });
                    mainButton.dispatch('keyup', { key: 'Enter' });
                }
                const finalOutcome = outcome;
                task.cleanup(); const pendingAfterCleanup = task._trackedTimeouts?.size || 0;
                taskSuccess = originalSuccess; taskFail = originalFail; gameActive = false; activeTaskObj = null;
                return { earlyOutcome, finalOutcome, unlocked, pendingAfterCleanup };
            },
            timeoutScenario: (id) => {
                const definition = TASK_REGISTRY.find((task) => task.id === id); const originalFail = taskFail; let failureKey = null;
                taskFail = (key) => { failureKey = key; };
                level = Math.max(10, definition.minLevel); timeLeft = Math.max(2, definition.minDuration); activeGlobalRule = null;
                gameActive = true; gameplayPaused = false; inputTransitionLocked = false;
                const task = definition.create(); activeTaskObj = task; task.setup(); task.onTimeUp(); task.cleanup();
                const pendingAfterCleanup = task._trackedTimeouts?.size || 0;
                taskFail = originalFail; gameActive = false; activeTaskObj = null; return { failureKey, pendingAfterCleanup };
            },
            staleTimeout: () => {
                let fired = false; const task = {}; scheduleTaskTimeout(task, () => { fired = true; }, 100); clearTaskTimeouts(task); return () => fired;
            },
            timeoutPause: () => {
                let fired = false; const task = {}; gameActive = true; activeTaskObj = task;
                scheduleTaskTimeout(task, () => { fired = true; }, 400); pauseTaskTimeouts(task);
                const paused = [...task._trackedTimeouts][0].paused && [...task._trackedTimeouts][0].remaining === 400;
                resumeTaskTimeouts(task); return { paused, fired: () => fired, task };
            },
            holdLeak: () => {
                let activations = 0; const next = { handleActivate() { activations += 1; } };
                const hold = { handlePressStart() {}, handlePressEnd() { activeTaskObj = next; } };
                gameActive = true; gameplayPaused = false; inputTransitionLocked = false; activeTaskObj = hold;
                mainButton.dispatch('pointerdown', { pointerId: 4 }); mainButton.dispatch('pointerup', { pointerId: 4 }); mainButton.dispatch('click');
                gameActive = false; activeTaskObj = null; return activations;
            },
            transitionInput: () => {
                let activations = 0; activeTaskObj = { handleActivate() { activations += 1; } };
                gameActive = true; gameplayPaused = false; inputTransitionLocked = true;
                mainButton.dispatch('pointerdown', { pointerId: 8 }); mainButton.dispatch('pointerup', { pointerId: 8 });
                gameActive = false; activeTaskObj = null; inputTransitionLocked = false; return activations;
            },
            scorePreview: (values) => {
                level = values.level; timeLeft = values.remaining; timerDuration = values.duration; combo = values.combo;
                activeTaskDefinition = { difficulty: values.difficulty }; activeModifiers = Array(values.modifiers).fill('small');
                activeGlobalRule = values.rule ? { id: 'invert' } : null; return calculateTaskScore();
            },
            comboScenario: () => {
                const originalNextLevel = nextLevel; nextLevel = () => {};
                level = 3; timeLeft = 3.5; timerDuration = 7; combo = 0; highestCombo = 0; sessionScore = 0;
                selectedMode = '7'; gameActive = true; activeTaskDefinition = { difficulty: 2 }; activeModifiers = []; activeGlobalRule = null; activeTaskObj = {};
                taskSuccess(); const afterOne = { combo, score: sessionScore }; gameActive = true; taskSuccess(); const afterTwo = { combo, score: sessionScore, highestCombo };
                gameActive = true; startTime = performance.now(); activeTaskObj = { cleanup() {} }; taskFail('failure.timeout');
                const reset = combo; nextLevel = originalNextLevel; gameActive = false; activeTaskObj = null;
                return { afterOne, afterTwo, reset };
            },
            updateBest: (mode, reachedLevel, score) => { selectedMode = mode; level = reachedLevel; sessionScore = score; sessionPersonalBest = false; personalBestAnnounced = false; updatePersonalBest(reachedLevel); return sessionPersonalBest; },
            retryClean: () => {
                gameActive = true; gameplayPaused = true; inputTransitionLocked = true; activeModifiers = ['moving']; activeGlobalRule = { id: 'invert' };
                taskHistory = ['hold']; activeTaskObj = { cleanup() { this.cleaned = true; } }; const oldTask = activeTaskObj; cleanSessionRuntime();
                return { cleaned: oldTask.cleaned, state: globalThis.reflex7Test.state() };
            },
            retrySession: () => {
                selectedMode = '4'; baseTime = 4; gameActive = false; activeModifiers = ['moving']; activeGlobalRule = { id: 'invert' };
                taskHistory = ['hold']; activeTaskObj = { cleanup() { this.cleaned = true; } }; const oldTask = activeTaskObj;
                document.getElementById('retry-button').dispatch('click');
                return { cleaned: oldTask.cleaned, state: globalThis.reflex7Test.state() };
            },
            pauseScenario: () => {
                const task = { pause() { this.paused = true; }, resume() { this.resumed = true; } };
                gameActive = true; gameplayPaused = false; inputTransitionLocked = false; activeTaskObj = task; timeLeft = 2; timerDeadline = performance.now() + 2000;
                const paused = pauseGame('manual'); return { task, paused, resume: () => resumeGame(), remaining: () => timeLeft };
            },
            visibility: () => {
                const task = { pause() { this.paused = true; } }; gameActive = true; gameplayPaused = false; inputTransitionLocked = false;
                activeTaskObj = task; timerDeadline = performance.now() + 2000; document.hidden = true; document.dispatch('visibilitychange');
                const result = { paused: gameplayPaused && task.paused, staysPaused: true }; document.hidden = false; document.dispatch('visibilitychange');
                result.staysPaused = gameplayPaused; cleanSessionRuntime(); return result;
            },
            audio: () => audio,
            toggleSound: (enabled) => audio.setEnabled(enabled),
            discover: (id) => storeDiscovery(id),
            dismissTip: () => document.getElementById('dismiss-tip').dispatch('click')
        };
    `;
    vm.runInContext(source + exports, context);
    return {
        context, timers, storage, elements, languageButtons, modeButtons, soundButtons,
        advance(ms) { now += ms; },
        runTimers(limit = Infinity) {
            const pending = [...timers.entries()].slice(0, limit); pending.forEach(([id, timer]) => { timers.delete(id); timer.callback(); });
        }
    };
}

const environment = createEnvironment();
const api = environment.context.reflex7Test;
const keys = api.keys();
assert.deepEqual([...keys.tr].sort(), [...keys.en].sort(), 'TR/EN dictionaries must have identical keys');
assert.equal(api.language(), 'tr', 'Turkish is the default language');
environment.languageButtons[1].dispatch('click');
assert.equal(api.language(), 'en', 'English can be selected');
assert.equal(createEnvironment({ storage: environment.storage }).context.reflex7Test.language(), 'en', 'language persists into a separate session');

const startGuard = createEnvironment({ storage: new Map([['reflex7_tip_seen', 'true']]) });
startGuard.modeButtons[0].dispatch('click');
startGuard.modeButtons[1].dispatch('click');
assert.equal(startGuard.context.reflex7Test.state().selectedMode, '7', 'a second mode click cannot replace a pending start');
assert(startGuard.modeButtons.every((button) => button.disabled), 'mode controls lock during the entrance transition');
startGuard.runTimers(1);
assert(startGuard.context.reflex7Test.state().gameActive, 'one guarded timer starts the session');

const registry = api.registry();
assert.equal(registry.length, 22, 'the registry contains 22 mechanics after the replacement');
assert.equal(new Set(registry.map((task) => task.id)).size, registry.length, 'task ids are unique');
assert(!registry.some((task) => task.id === ['rhy', 'thm'].join('')), 'the removed mechanic has no registry entry');
for (const id of ['wait', 'lastSecondInstruction', 'patienceCountdown']) assert(registry.some((task) => task.id === id), `${id}: registered`);
for (const id of ['wait', 'lastSecondInstruction', 'patienceCountdown']) {
    const definition = registry.find((task) => task.id === id);
    assert(definition.inputs.includes('pointer') && definition.inputs.includes('keyboard'), `${id}: pointer and keyboard metadata`);
    assert(definition.modifiers.length > 0 && definition.rules.length > 0, `${id}: modifier and global-rule compatibility metadata`);
    const lifecycle = definition.create();
    assert.equal(typeof lifecycle.pause, 'function', `${id}: pause lifecycle`);
    assert.equal(typeof lifecycle.resume, 'function', `${id}: resume lifecycle`);
}

api.setState({ level: 50, duration: 2, memory: {} });
assert(!api.weights().some((task) => task.id === 'previousMemory'), 'memory task requires valid memory');
api.setState({ level: 50, duration: 2, memory: { previousTarget: 3 } });
assert(api.weights().some((task) => task.id === 'previousMemory'), 'memory task unlocks with valid memory');
api.setState({ level: 50, duration: 2, memory: { previousTarget: 3 }, rule: null, tasks: [], categories: [] });
for (const id of ['wait', 'lastSecondInstruction', 'patienceCountdown']) assert(api.weights().some((task) => task.id === id), `${id}: selectable`);

api.setState({ level: 50, duration: 2, memory: { previousTarget: 3 }, tasks: [], categories: [] });
const baseStandard = api.weights().find((task) => task.id === 'standard').selectionWeight;
api.setState({ level: 50, duration: 2, memory: { previousTarget: 3 }, tasks: ['standard', 'hold'], categories: ['reaction', 'timing'] });
assert(api.weights().find((task) => task.id === 'standard').selectionWeight < baseStandard, 'recent task penalty reduces weight');
api.setState({ level: 50, duration: 2, memory: { previousTarget: 3 }, tasks: ['hold'], categories: ['arithmetic'] });
const categoryPenalizedParity = api.weights().find((task) => task.id === 'parity').selectionWeight;
api.setState({ level: 50, duration: 2, memory: { previousTarget: 3 }, tasks: ['hold'], categories: ['timing'] });
assert(categoryPenalizedParity < api.weights().find((task) => task.id === 'parity').selectionWeight, 'recent category penalty reduces weight');

api.setState({ level: 50, duration: 1.2, memory: { previousTarget: 3 } });
const availableAtShortDuration = new Set(api.weights().map((task) => task.id));
registry.filter((task) => task.minDuration > 1.2).forEach((task) => assert(!availableAtShortDuration.has(task.id), `${task.id}: duration gate`));

const selected = api.selectMany(300, 123456);
for (let index = 1; index < selected.length; index += 1) assert.notEqual(selected[index], selected[index - 1], 'tasks never immediately repeat');

for (const rule of ['invert', 'ignoreRed', 'finalLine', 'oddWait', 'emojiLiteral']) {
    api.setState({ level: 50, duration: 2, memory: { previousTarget: 3 }, rule: { id: rule, remaining: 2 } });
    assert(api.weights().every((task) => task.rules.includes(rule)), `global rule compatibility: ${rule}`);
}

for (const definition of registry) {
    const { task, timeoutMaximum } = api.instantiate(definition.id, 0.42);
    assert.equal(typeof task.setup, 'function', `${definition.id}: setup`);
    assert.equal(typeof task.onTimeUp, 'function', `${definition.id}: timeout`);
    assert.equal(typeof task.cleanup, 'function', `${definition.id}: cleanup`);
    assert(timeoutMaximum < (definition.minDuration * 1000), `${definition.id}: internal delay leaves completion time`);
}

for (const id of ['oddOneOut', 'numberExtremum', 'parity', 'stroop', 'previousMemory', 'fakeButton', 'positionMemory', 'countSymbols', 'yesNo', 'lastSecondInstruction']) {
    const result = api.instantiate(id, 0.42);
    assert(result.choiceCount >= 2, `${id}: visible choices`);
    assert.equal(result.correctCount, 1, `${id}: exactly one unambiguous answer`);
}

for (const id of ['wait', 'lastSecondInstruction', 'patienceCountdown']) {
    for (const randomValue of [0, 0.49, 0.51, 0.999]) {
        for (const duration of [1.5, 2, 4, 7]) {
            const definition = registry.find((task) => task.id === id);
            if (duration < definition.minDuration) continue;
            const { task, timeoutMaximum } = api.instantiate(id, randomValue, { duration, level: 50 });
            const minimumReserve = id === 'wait' ? 0.5 : id === 'lastSecondInstruction' ? 0.58 : 0.48;
            const signalDelay = id === 'lastSecondInstruction' ? task.changeDelay : task.signalDelay;
            assert(signalDelay <= duration - minimumReserve + Number.EPSILON, `${id}: GO/final signal leaves a safe response reserve`);
            assert(timeoutMaximum <= signalDelay * 1000 + Number.EPSILON, `${id}: tracked delay is bounded by its signal time`);
        }
    }
}

const waitScenario = api.gateScenario('wait', 0.4);
assert.deepEqual(waitScenario.earlyOutcome, { type: 'failure', key: 'failure.waitEarly' }, 'wait: pressing before permission fails clearly');
assert(waitScenario.unlocked && waitScenario.finalOutcome.type === 'success', 'wait: pressing after permission succeeds');
assert.equal(waitScenario.pendingAfterCleanup, 0, 'wait: cleanup removes tracked timers');
const lastSecondScenario = api.gateScenario('lastSecondInstruction', 0.4);
assert.equal(lastSecondScenario.earlyOutcome, null, 'last-second task completely locks early input');
assert(lastSecondScenario.unlocked && lastSecondScenario.finalOutcome.type === 'success', 'last-second task judges the final instruction only');
assert.equal(lastSecondScenario.pendingAfterCleanup, 0, 'last-second task cleanup removes tracked timers');
const patienceScenario = api.gateScenario('patienceCountdown', 0.9);
assert.deepEqual(patienceScenario.earlyOutcome, { type: 'failure', key: 'failure.patienceEarly' }, 'patience: pressing before GO fails clearly');
assert(patienceScenario.unlocked && patienceScenario.finalOutcome.type === 'success', 'patience: pressing after GO succeeds');
assert.equal(patienceScenario.pendingAfterCleanup, 0, 'patience cleanup removes tracked timers');
for (const [id, failureKey] of [['wait', 'failure.waitTimeout'], ['lastSecondInstruction', 'failure.lastSecondTimeout'], ['patienceCountdown', 'failure.patienceTimeout']]) {
    const timeout = api.timeoutScenario(id);
    assert.equal(timeout.failureKey, failureKey, `${id}: timeout has a precise failure reason`);
    assert.equal(timeout.pendingAfterCleanup, 0, `${id}: timeout cleanup leaves no tracked timer`);
}

for (const definition of registry.filter((task) => task.modifiers.length)) {
    const modifiers = api.modifiersFor(definition.id, 7);
    assert(modifiers.length <= 2, 'advanced band has at most two modifiers');
    assert(modifiers.every((modifier) => definition.modifiers.includes(modifier)), `${definition.id}: modifier compatibility`);
    assert(!(modifiers.includes('moving') && modifiers.includes('swap')), 'moving and swap do not combine');
}

assert.equal(api.scorePreview({ level: 10, remaining: 3.5, duration: 7, combo: 1, difficulty: 2, modifiers: 1, rule: true }), 285, 'documented score formula is deterministic');
assert.equal(api.scorePreview({ level: 10, remaining: 2, duration: 4, combo: 1, difficulty: 2, modifiers: 1, rule: true }), 285, 'normalized time makes modes comparable');
const combo = api.comboScenario();
assert.equal(combo.afterOne.combo, 1, 'first success starts combo');
assert.equal(combo.afterTwo.combo, 2, 'consecutive success increases combo');
assert(combo.afterTwo.score > combo.afterOne.score * 2, 'combo multiplier affects session score');
assert.equal(combo.reset, 0, 'failure reset state is zero');

const recordsStorage = new Map([['reflex7_best', '17']]);
const migrated = createEnvironment({ storage: recordsStorage });
assert.equal(migrated.context.reflex7Test.records()['7'].level, 17, 'legacy best migrates to slow mode');
assert.equal(migrated.context.reflex7Test.records()['4'].level, 17, 'legacy best migrates to fast mode');
assert(migrated.context.reflex7Test.updateBest('7', 18, 500), 'new record is detected');
assert(!migrated.context.reflex7Test.updateBest('4', 10, 0), 'lower record is not marked as new');
assert(migrated.context.reflex7Test.updateBest('4', 10, 100), 'independent fast-mode score can set a record');
assert.equal(migrated.context.reflex7Test.records()['7'].score, 500, 'slow score is stored separately');
assert.equal(migrated.context.reflex7Test.records()['4'].score, 100, 'fast score is stored separately');
assert.doesNotThrow(() => createEnvironment({ blockStorage: true }), 'storage-denied browsers remain usable');

assert.equal(api.holdLeak(), 0, 'hold release cannot activate the next task');
assert.equal(api.transitionInput(), 0, 'input remains locked during transitions');
const didFire = api.staleTimeout(); environment.runTimers();
assert.equal(didFire(), false, 'cleared task timeout cannot affect a later task');
const timeoutPause = api.timeoutPause();
assert(timeoutPause.paused, 'tracked task timeout preserves its remaining duration when paused');
environment.runTimers(); assert.equal(timeoutPause.fired(), true, 'tracked timeout resumes exactly once');

const paused = api.pauseScenario();
assert(paused.paused && paused.task.paused, 'manual pause freezes task and main timer');
environment.advance(5000);
assert(paused.resume(), 'manual resume succeeds while visible');
assert(paused.remaining() > 1.9, 'paused wall time does not consume task time');
environment.runTimers(1);
assert(paused.task.resumed, 'task lifecycle resumes after the input guard');
assert.deepEqual(api.visibility(), { paused: true, staysPaused: true }, 'background tab pauses and never auto-resumes');

const retry = api.retryClean();
assert(retry.cleaned, 'retry cleanup calls the active task cleanup path');
assert(!retry.state.gameActive && !retry.state.gameplayPaused && !retry.state.transitionLocked, 'retry cleanup resets session flags');
assert.equal(retry.state.activeModifiers.length, 0, 'retry cleanup removes modifiers');
assert.equal(retry.state.activeGlobalRule, null, 'retry cleanup removes global rules');
assert.equal(retry.state.taskHistory.length, 0, 'retry cleanup removes task history');
const restarted = api.retrySession();
assert(restarted.cleaned && restarted.state.gameActive && restarted.state.level === 1, 'same-mode retry starts a clean level-one session without reload');
assert.equal(restarted.state.selectedMode, '4', 'retry preserves the selected mode');
assert.equal(restarted.state.activeModifiers.length, 0, 'old retry modifier state does not survive');
assert.equal(restarted.state.activeGlobalRule, null, 'old retry rule state does not survive');

api.audio().unlock();
assert(api.audio().play('success'), 'audio unlocks after an explicit interaction');
assert(!api.audio().play('success'), 'audio cooldown prevents same-sound flooding');
assert(api.audio().channels.size <= 1, 'feedback audio uses one managed channel');
api.toggleSound(false);
assert.equal(api.state().soundEnabled, false, 'sound can be disabled');
assert.equal(api.audio().channels.size, 0, 'disabling sound stops active channels');
assert.equal(createEnvironment({ storage: environment.storage }).context.reflex7Test.state().soundEnabled, false, 'sound preference persists');

api.discover('task:hold');
assert(environment.storage.get('reflex7_discoveries').includes('task:hold'), 'mechanic discovery persists');
api.dismissTip();
assert.equal(environment.storage.get('reflex7_tip_seen'), 'true', 'first-run tip dismissal persists');
assert.equal(createEnvironment({ storage: environment.storage }).context.reflex7Test.state().tipSeen, true, 'onboarding stays dismissed next session');

const manifest = JSON.parse(fs.readFileSync('manifest.webmanifest', 'utf8'));
for (const icon of manifest.icons) assert(fs.existsSync(icon.src), `manifest icon exists: ${icon.src}`);
const serviceWorker = fs.readFileSync('sw.js', 'utf8');
const cachedAssets = [...serviceWorker.matchAll(/'\.\/([^']+)'/g)].map((match) => match[1]).filter(Boolean);
for (const asset of cachedAssets) assert(fs.existsSync(asset), `service-worker asset exists: ${asset}`);
assert(serviceWorker.includes('reflex7-v1.1.0'), 'service worker uses a versioned cache');

const css = fs.readFileSync('style.css', 'utf8');
assert(css.includes('prefers-reduced-motion: reduce'), 'reduced-motion styles exist');
assert(css.includes('@media (max-width: 380px)'), 'very narrow viewport fallback exists');
assert(css.includes('@media (max-height: 620px)'), '568px-tall viewport is covered by short-screen constraints');

const html = fs.readFileSync('index.html', 'utf8');
assert(!fs.readFileSync('script.js', 'utf8').includes('innerHTML'), 'runtime never inserts nickname or task text with innerHTML');
const htmlIds = new Set([...html.matchAll(/\sid="([^"]+)"/g)].map((match) => match[1]));
const translationKeys = new Set(keys.tr);
for (const match of html.matchAll(/data-i18n(?:-placeholder|-aria-label)?="([^"]+)"/g)) {
    assert(translationKeys.has(match[1]), `HTML translation key exists: ${match[1]}`);
}
for (const id of [...fs.readFileSync('script.js', 'utf8').matchAll(/getElementById\('([^']+)'\)/g)].map((match) => match[1])) {
    assert(htmlIds.has(id), `script DOM reference exists in HTML: #${id}`);
}

console.log(`Reflex7 v1.1.0 tests passed: ${registry.length} tasks, ${keys.tr.length} keys/locale, 300 controlled selections, session/PWA checks.`);
