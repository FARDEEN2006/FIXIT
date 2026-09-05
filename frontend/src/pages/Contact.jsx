import React from "react";
import {
  Instagram,
  Mail,
  MapPin,
  MessageCircle,
  Phone,
  Youtube,
} from "lucide-react";
import SEO from "../components/common/SEO";
import "../styles/contact.css";

const phoneNumber = "8870783647";
const storeAddress =
  "Ground Floor, Shop No. 7 & 34, Perris Plaza, Municipality Building, Anna Salai (Backside of the Bus Stand), Karur, Tamil Nadu - 639001";
const mapsUrl =
  "https://www.google.com/maps/search/?api=1&query=Ground+Floor%2C+Shop+No.+7+%26+34%2C+Perris+Plaza%2C+Municipality+Building%2C+Anna+Salai+%28Backside+of+the+Bus+Stand%29%2C+Karur%2C+Tamil+Nadu+-+639001";
const instagramUrl =
  "https://www.instagram.com/fixit_the_repair_company?igsi=ZHd1anhxaXBlY3Zo";

export default function Contact() {
  return (
    <main className="section">
      <div className="container">
        <div className="section-header fixit-contact-page">
          <SEO
            title="Contact FIXIT Karur"
            path="/contact"
            description="Contact FIXIT Mobile Sales & Services in Karur for mobile sales, repair, and service."
          />
          <span className="section-label">Contact</span>
          <h1>Talk to FIXIT</h1>
          <div className="fixit-contact-actions">
            <a href={`tel:+91${phoneNumber}`} aria-label="Call FIXIT">
              <Phone size={20} />
            </a>
            <a
              href={`https://wa.me/91${phoneNumber}`}
              target="_blank"
              rel="noopener noreferrer"
              aria-label="Contact FIXIT on WhatsApp"
            >
              <MessageCircle size={20} />
            </a>
            <a
              href={instagramUrl}
              target="_blank"
              rel="noopener noreferrer"
              aria-label="Visit FIXIT on Instagram"
            >
              <Instagram size={20} />
            </a>
            <a
              href="https://youtube.com/@fixit3927?si=808a0F-Pf-ce8EhK"
              target="_blank"
              rel="noopener noreferrer"
              aria-label="Visit FIXIT on YouTube"
            >
              <Youtube size={20} />
            </a>
          </div>
          <address>
            <p>
              <a href={mapsUrl} target="_blank" rel="noopener noreferrer">
                <MapPin size={18} /> {storeAddress}
              </a>
            </p>
            <p>
              <a href={`tel:+91${phoneNumber}`}>{phoneNumber}</a>
              {" · "}
              <a href="mailto:fixitmobileservicess@gmail.com">
                <Mail size={18} /> fixitmobileservicess@gmail.com
              </a>
            </p>
          </address>
        </div>
      </div>
    </main>
  );
}
