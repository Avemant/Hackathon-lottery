const COMBINATION_SIZE = 6;
const MIN = 1;
const MAX = 49;

export function parseCombination(input: string): number[] | string {
  const parts = input
    .split(/[\s,;]+/)
    .map((s) => s.trim())
    .filter(Boolean);

  if (parts.length !== COMBINATION_SIZE) {
    return `Введите ровно ${COMBINATION_SIZE} чисел (1–${MAX}), через запятую или пробел`;
  }

  const numbers: number[] = [];
  const seen = new Set<number>();

  for (const part of parts) {
    const n = Number(part);
    if (!Number.isInteger(n) || n < MIN || n > MAX) {
      return `Число «${part}» должно быть целым от ${MIN} до ${MAX}`;
    }
    if (seen.has(n)) {
      return 'Числа не должны повторяться';
    }
    seen.add(n);
    numbers.push(n);
  }

  return numbers;
}

export function formatNumbers(nums: number[] | null | undefined): string {
  if (!nums?.length) return '—';
  return [...nums].sort((a, b) => a - b).join(', ');
}
