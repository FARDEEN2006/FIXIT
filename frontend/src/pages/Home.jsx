import React from "react";

import Hero from "../components/home/Hero";
import SellMobileCTA from "../components/home/SellMobileCTA";
import WhyChooseUs from "../components/home/WhyChooseUs";
import StoreInfoSection from "../components/home/StoreInfoSection";
import SEO from "../components/common/SEO";

import "../styles/home.css";

function Home() {
  return (
    <main className="fixit-home">
      <SEO
        title="Mobile Sales & Repair in Karur"
        path="/"
        description="FIXIT Karur is your local mobile shop for phone sales, mobile repair, and trusted mobile services."
      />
      <Hero />

      <SellMobileCTA />

      <WhyChooseUs />

      <StoreInfoSection />
    </main>
  );
}

export default Home;