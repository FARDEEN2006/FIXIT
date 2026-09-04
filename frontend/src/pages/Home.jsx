import React from "react";

import Hero from "../components/home/Hero";
import SellMobileCTA from "../components/home/SellMobileCTA";
import WhyChooseUs from "../components/home/WhyChooseUs";
import StoreInfoSection from "../components/home/StoreInfoSection";

import "../styles/home.css";

function Home() {
  return (
    <main className="fixit-home">
      <Hero />

      <SellMobileCTA />

      <WhyChooseUs />

      <StoreInfoSection />
    </main>
  );
}

export default Home;