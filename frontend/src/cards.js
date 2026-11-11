export function onlyDigits(text) {
  return text.replace(/[^0-9]/g, "");
}

export function isValidLuhn(cardNumber) {
  const digits = onlyDigits(cardNumber);
  if (digits.length < 13 || digits.length > 19) {
    return false;
  }
  let sum = 0;
  let doubleIt = false;
  for (let i = digits.length - 1; i >= 0; i--) {
    let digit = Number(digits[i]);
    if (doubleIt) {
      digit = digit * 2;
      if (digit > 9) {
        digit = digit - 9;
      }
    }
    sum = sum + digit;
    doubleIt = !doubleIt;
  }
  return sum % 10 === 0;
}

export function formatCardNumber(text) {
  const digits = onlyDigits(text).slice(0, 19);
  const groups = digits.match(/.{1,4}/g);
  return groups ? groups.join(" ") : "";
}

export function detectBrand(cardNumber) {
  const digits = onlyDigits(cardNumber);
  if (digits.startsWith("4")) return "Visa";
  if (digits.startsWith("5") || digits.startsWith("2")) return "Mastercard";
  if (digits.startsWith("34") || digits.startsWith("37")) return "Amex";
  return "";
}

export function isExpired(month, year) {
  const now = new Date();
  const currentYear = now.getFullYear();
  const currentMonth = now.getMonth() + 1;
  return year < currentYear || (year === currentYear && month < currentMonth);
}

export function checkCard(card) {
  const errors = {};
  if (!isValidLuhn(card.cardNumber)) {
    errors.cardNumber = "Неверный номер карты";
  }
  if (!/^[A-Za-z \-]{2,100}$/.test(card.holderName.trim())) {
    errors.holderName = "Имя владельца латиницей, как на карте";
  }
  if (isExpired(Number(card.expMonth), Number(card.expYear))) {
    errors.expMonth = "Срок действия карты истёк";
  }
  if (!/^[0-9]{3,4}$/.test(card.cvv)) {
    errors.cvv = "CVV: 3 или 4 цифры";
  }
  return errors;
}

export function emptyCard() {
  const now = new Date();
  return {
    cardNumber: "",
    holderName: "",
    expMonth: String(now.getMonth() + 1),
    expYear: String(now.getFullYear() + 2),
    cvv: ""
  };
}

export function cardForServer(card) {
  return {
    cardNumber: onlyDigits(card.cardNumber),
    holderName: card.holderName.trim(),
    expMonth: Number(card.expMonth),
    expYear: Number(card.expYear),
    cvv: card.cvv
  };
}
