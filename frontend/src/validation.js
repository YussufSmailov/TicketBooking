export const EMAIL_PATTERN = "[A-Za-z0-9._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,}";
export const PHONE_PATTERN = "\\+?[0-9]{10,15}";
export const NAME_PATTERN = "[\\p{L} \\-]{2,100}";

export const EMAIL_HINT = "Email в формате name@example.com";
export const PHONE_HINT = "От 10 до 15 цифр, можно с + в начале";
export const NAME_HINT = "Только буквы, пробелы и дефис, от 2 до 100 символов";

export function todayString() {
  const now = new Date();
  const month = String(now.getMonth() + 1).padStart(2, "0");
  const day = String(now.getDate()).padStart(2, "0");
  return `${now.getFullYear()}-${month}-${day}`;
}

export const PASSWORD_PATTERN = "(?=.*[A-Za-zА-Яа-яЁё])(?=.*[0-9]).{8,72}";
export const PASSWORD_HINT = "Минимум 8 символов, буквы и цифры";
