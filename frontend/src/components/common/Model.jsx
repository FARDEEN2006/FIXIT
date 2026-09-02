import React, { useEffect } from "react";
import { X } from "lucide-react";

function Modal({
  isOpen,
  onClose,
  title,
  children,
  size = "medium",
}) {
  useEffect(() => {
    if (!isOpen) return;

    const handleEscape = (event) => {
      if (event.key === "Escape") {
        onClose();
      }
    };

    document.addEventListener("keydown", handleEscape);

    document.body.style.overflow = "hidden";

    return () => {
      document.removeEventListener("keydown", handleEscape);
      document.body.style.overflow = "";
    };
  }, [isOpen, onClose]);

  if (!isOpen) {
    return null;
  }

  return (
    <div
      className="fixit-modal-overlay"
      onMouseDown={(event) => {
        if (event.target === event.currentTarget) {
          onClose();
        }
      }}
    >
      <div
        className={`fixit-modal fixit-modal-${size}`}
        role="dialog"
        aria-modal="true"
        aria-labelledby="fixit-modal-title"
      >
        <div className="fixit-modal-header">
          <h2 id="fixit-modal-title">
            {title}
          </h2>

          <button
            type="button"
            className="fixit-modal-close"
            onClick={onClose}
            aria-label="Close modal"
          >
            <X size={22} />
          </button>
        </div>

        <div className="fixit-modal-body">
          {children}
        </div>
      </div>
    </div>
  );
}

export default Modal;