import React from "react";

import Hero from "../components/home/Hero";
import ServicesSection from "../components/home/ServicesSection";
import FeaturedProducts from "../components/home/FeaturedProducts";
import SellMobileCTA from "../components/home/SellMobileCTA";
import WhyChooseUs from "../components/home/WhyChooseUs";
import StoreInfoSection from "../components/home/StoreInfoSection";

import "../styles/home.css";

function Home() {
  return (
    <main className="fixit-home">
      <Hero />

      <ServicesSection />

      <FeaturedProducts />

      <SellMobileCTA />

      <WhyChooseUs />

      <StoreInfoSection />
    </main>
  );
}

export default Home;