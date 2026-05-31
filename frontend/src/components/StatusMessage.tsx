type StatusMessageProps = {
  variant: 'error' | 'success' | 'info';
  message: string;
  id?: string;
};

export function StatusMessage({ variant, message, id }: StatusMessageProps) {
  return (
    <p id={id} className={`status status--${variant}`} role={variant === 'error' ? 'alert' : 'status'}>
      {message}
    </p>
  );
}
