import React, { useEffect, useState } from "react";
import { getProducts, getServices } from "../services/api";

import Hero from "../components/home/Hero";
import ServicesSection from "../components/home/ServicesSection";
import FeaturedProducts from "../components/home/FeaturedProducts";
import SellMobileCTA from "../components/home/SellMobileCTA";
import WhyChooseUs from "../components/home/WhyChooseUs";
import StoreInfoSection from "../components/home/StoreInfoSection";

import "../styles/home.css";

function Home() {
  const [products, setProducts] = useState([]);
  const [services, setServices] = useState([]);
  useEffect(() => { getProducts(0, 4).then(setProducts).catch(() => {}); getServices().then(setServices).catch(() => {}); }, []);
  return (
    <main className="fixit-home">
      <Hero />

      <ServicesSection services={services} />

      <FeaturedProducts products={products} />

      <SellMobileCTA />

      <WhyChooseUs />

      <StoreInfoSection />
    </main>
  );
}

export default Home;
