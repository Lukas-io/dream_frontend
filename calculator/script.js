let currentInput = '0';
let previousInput = '';
let operator = null;
let shouldResetDisplay = false;

const displayCurrent = document.getElementById('displayCurrent');
const displayPrevious = document.getElementById('displayPrevious');
const operatorButtons = document.querySelectorAll('.btn-operator');

const operatorSymbols = { '/': '÷', '*': '×', '-': '−', '+': '+' };

function formatNumber(value) {
  if (value === 'Error') return 'Error';
  const num = parseFloat(value);
  if (isNaN(num)) return value;
  if (Math.abs(num) >= 1e12) return num.toExponential(4);
  const str = value.toString();
  if (str.includes('.')) {
    const [intPart, decPart] = str.split('.');
    return parseFloat(intPart).toLocaleString('en-US') + '.' + decPart;
  }
  return num.toLocaleString('en-US');
}

function updateDisplay() {
  displayCurrent.textContent = formatNumber(currentInput);
  if (operator && previousInput) {
    displayPrevious.textContent = formatNumber(previousInput) + ' ' + operatorSymbols[operator];
  } else {
    displayPrevious.textContent = '';
  }
  operatorButtons.forEach(btn => {
    btn.classList.toggle('active', btn.dataset.operator === operator && shouldResetDisplay);
  });
}

function handleNumber(value) {
  if (currentInput === 'Error') currentInput = '0';
  if (value === '.') {
    if (shouldResetDisplay) {
      currentInput = '0.';
      shouldResetDisplay = false;
      updateDisplay();
      return;
    }
    if (currentInput.includes('.')) return;
    currentInput += '.';
    updateDisplay();
    return;
  }
  if (shouldResetDisplay) {
    currentInput = value;
    shouldResetDisplay = false;
  } else if (currentInput === '0') {
    currentInput = value;
  } else {
    if (currentInput.replace(/[^0-9]/g, '').length >= 15) return;
    currentInput += value;
  }
  updateDisplay();
}

function calculate() {
  const prev = parseFloat(previousInput);
  const curr = parseFloat(currentInput);
  if (isNaN(prev) || isNaN(curr)) return;
  let result;
  switch (operator) {
    case '+': result = prev + curr; break;
    case '-': result = prev - curr; break;
    case '*': result = prev * curr; break;
    case '/':
      if (curr === 0) {
        currentInput = 'Error';
        previousInput = '';
        operator = null;
        shouldResetDisplay = true;
        updateDisplay();
        return;
      }
      result = prev / curr;
      break;
    default: return;
  }
  result = Math.round(result * 1e12) / 1e12;
  currentInput = result.toString();
  previousInput = '';
  operator = null;
  shouldResetDisplay = true;
  updateDisplay();
}

function handleOperator(op) {
  if (currentInput === 'Error') return;
  if (operator && !shouldResetDisplay) {
    calculate();
    if (currentInput === 'Error') return;
  }
  previousInput = currentInput;
  operator = op;
  shouldResetDisplay = true;
  updateDisplay();
}

function handleSpecial(action) {
  switch (action) {
    case 'clear':
      currentInput = '0';
      previousInput = '';
      operator = null;
      shouldResetDisplay = false;
      updateDisplay();
      break;
    case 'delete':
      if (currentInput === 'Error') {
        currentInput = '0';
      } else if (currentInput.length === 1 || (currentInput.length === 2 && currentInput.startsWith('-'))) {
        currentInput = '0';
      } else {
        currentInput = currentInput.slice(0, -1);
      }
      updateDisplay();
      break;
    case 'percent':
      if (currentInput === 'Error') return;
      currentInput = (parseFloat(currentInput) / 100).toString();
      updateDisplay();
      break;
    case 'equals':
      if (operator) calculate();
      break;
  }
}

document.querySelector('.buttons').addEventListener('click', (e) => {
  const btn = e.target.closest('.btn');
  if (!btn) return;
  if (btn.dataset.number !== undefined) handleNumber(btn.dataset.number);
  else if (btn.dataset.operator) handleOperator(btn.dataset.operator);
  else if (btn.dataset.action) handleSpecial(btn.dataset.action);
});

document.addEventListener('keydown', (e) => {
  if (e.key >= '0' && e.key <= '9') handleNumber(e.key);
  else if (e.key === '.') handleNumber('.');
  else if (e.key === '+') handleOperator('+');
  else if (e.key === '-') handleOperator('-');
  else if (e.key === '*') handleOperator('*');
  else if (e.key === '/') { e.preventDefault(); handleOperator('/'); }
  else if (e.key === 'Enter' || e.key === '=') handleSpecial('equals');
  else if (e.key === 'Escape') handleSpecial('clear');
  else if (e.key === '%') handleSpecial('percent');
  else if (e.key === 'Backspace') handleSpecial('delete');
});
