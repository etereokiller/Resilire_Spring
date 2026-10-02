export function isValidRut(value) {
  const normalized = value.replace(/[^0-9kK]/g, "").toUpperCase();
  if (normalized.length < 2) return false;
  const body = normalized.slice(0, -1);
  const verifier = normalized.at(-1);
  if (!/^\d+$/.test(body)) return false;
  let sum = 0; let factor = 2;
  for (let index = body.length - 1; index >= 0; index -= 1) { sum += Number(body[index]) * factor; factor = factor === 7 ? 2 : factor + 1; }
  const expected = 11 - (sum % 11);
  return verifier === (expected === 11 ? "0" : expected === 10 ? "K" : String(expected));
}
