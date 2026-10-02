/** Shared use of the user-supplied, cropped Resilire logo asset. */
export default function ResiLogo({ width = 140, className }) {
  return (
    <img
      src="/resilire-logo-transparent.png"
      width={width}
      height={Math.round(width * 0.473)}
      className={className}
      alt="Resilire"
    />
  );
}
