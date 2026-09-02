import React from "react";
import { ArrowRight } from "lucide-react";

function Button({
  children,
  type = "button",
  variant = "primary",
  href,
  onClick,
  icon = true,
  disabled = false,
  className = "",
}) {
  const classes = `fixit-button fixit-button-${variant} ${className}`;

  if (href) {
    return (
      <a
        href={href}
        className={classes}
        onClick={onClick}
      >
        <span>{children}</span>

        {icon && <ArrowRight size={18} strokeWidth={2.2} />}
      </a>
    );
  }

  return (
    <button
      type={type}
      className={classes}
      onClick={onClick}
      disabled={disabled}
    >
      <span>{children}</span>

      {icon && <ArrowRight size={18} strokeWidth={2.2} />}
    </button>
  );
}

export default Button;