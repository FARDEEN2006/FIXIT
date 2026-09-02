import React, { useState } from "react";
import {
  Mail,
  MessageCircle,
  Phone,
  Send,
  User,
} from "lucide-react";

function ProductEnquiryForm({ product }) {
  const [formData, setFormData] = useState({
    name: "",
    phone: "",
    email: "",
  });

  const [error, setError] = useState("");

  const handleChange = (event) => {
    const { name, value } = event.target;

    setFormData((previous) => ({
      ...previous,
      [name]: value,
    }));

    setError("");
  };

  const handleSubmit = (event) => {
    event.preventDefault();

    const name = formData.name.trim();
    const phone = formData.phone.trim();
    const email = formData.email.trim();

    if (!name || !phone || !email) {
      setError("Please fill in all fields.");
      return;
    }

    if (!/^[0-9+\-\s()]{7,20}$/.test(phone)) {
      setError("Please enter a valid phone number.");
      return;
    }

    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
      setError("Please enter a valid email address.");
      return;
    }

    const message = [
      "Hello FIXIT Mobile Sales & Services,",
      "",
      "I am interested in the following product:",
      "",
      `Product: ${product.name}`,
      `Price: ₹${Number(product.price || 0).toLocaleString("en-IN")}`,
      "",
      "Customer Details:",
      `Name: ${name}`,
      `Phone: ${phone}`,
      `Email: ${email}`,
      "",
      "Please provide more details about availability.",
    ].join("\n");

    const whatsappNumber =
      import.meta.env.VITE_WHATSAPP_NUMBER;

    if (!whatsappNumber) {
      setError(
        "WhatsApp number is not configured yet. Please contact FIXIT."
      );
      return;
    }

    const cleanNumber =
      whatsappNumber.replace(/\D/g, "");

    const whatsappUrl =
      `https://wa.me/${cleanNumber}?text=${encodeURIComponent(message)}`;

    window.open(
      whatsappUrl,
      "_blank",
      "noopener,noreferrer"
    );
  };

  return (
    <form
      className="fixit-product-enquiry-form"
      onSubmit={handleSubmit}
    >

      <div className="fixit-enquiry-heading">

        <div className="fixit-enquiry-icon">
          <MessageCircle size={21} />
        </div>

        <div>
          <h2>
            Enquire About This Product
          </h2>

          <p>
            Enter your details and continue to WhatsApp.
          </p>
        </div>

      </div>

      <div className="fixit-form-field">

        <label htmlFor="product-name">
          Name
        </label>

        <div className="fixit-input-wrapper">

          <User size={17} />

          <input
            id="product-name"
            name="name"
            type="text"
            value={formData.name}
            onChange={handleChange}
            placeholder="Your name"
            autoComplete="name"
            required
          />

        </div>

      </div>

      <div className="fixit-form-field">

        <label htmlFor="product-phone">
          Phone
        </label>

        <div className="fixit-input-wrapper">

          <Phone size={17} />

          <input
            id="product-phone"
            name="phone"
            type="tel"
            value={formData.phone}
            onChange={handleChange}
            placeholder="Your phone number"
            autoComplete="tel"
            required
          />

        </div>

      </div>

      <div className="fixit-form-field">

        <label htmlFor="product-email">
          Email
        </label>

        <div className="fixit-input-wrapper">

          <Mail size={17} />

          <input
            id="product-email"
            name="email"
            type="email"
            value={formData.email}
            onChange={handleChange}
            placeholder="you@example.com"
            autoComplete="email"
            required
          />

        </div>

      </div>

      {error && (
        <p
          className="fixit-form-error"
          role="alert"
        >
          {error}
        </p>
      )}

      <button
        type="submit"
        className="fixit-button fixit-button-whatsapp"
      >
        <MessageCircle size={18} />
        <span>Continue to WhatsApp</span>
        <Send size={16} />
      </button>

      <p className="fixit-enquiry-note">
        No account or OTP is required for product enquiries.
      </p>

    </form>
  );
}

export default ProductEnquiryForm;