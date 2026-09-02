import React from "react";
import { Routes, Route } from "react-router-dom";

import Navbar from "./components/layout/Navbar";
import Footer from "./components/layout/Footer";
import ScrollToTop from "./components/layout/ScrollToTop";

import Home from "./pages/Home";
import Products from "./pages/Products";
import ProductDetails from "./pages/ProductDetails";
import SellYourMobile from "./pages/SellYourMobile";
import About from "./pages/About";
import Contact from "./pages/Contact";
import AdminLogin from "./pages/AdminLogin";
import VerifyEmail from "./pages/VerifyEmail";

function PlaceholderPage({ title }) {
  return (
    <main
      style={{
        minHeight: "70vh",
        display: "grid",
        placeItems: "center",
        padding: "80px 20px",
        textAlign: "center",
      }}
    >
      <div>
        <p
          style={{
            color: "#00AAAA",
            fontWeight: 700,
            marginBottom: "10px",
          }}
        >
          FIXIT MOBILE SALES & SERVICES
        </p>

        <h1
          style={{
            color: "#1B263B",
            marginBottom: "15px",
          }}
        >
          {title}
        </h1>

        <p>
          This section will be built in the next frontend block.
        </p>
      </div>
    </main>
  );
}

function App() {
  return (
    <>
      <ScrollToTop />

      <Navbar />

      <Routes>
        {/* HOME */}
        <Route
          path="/"
          element={<Home />}
        />

        {/* PRODUCTS */}
        <Route
          path="/products"
          element={<Products />}
        />

        {/* PRODUCT DETAILS */}
        <Route
          path="/products/:id"
          element={<ProductDetails />}
        />

        {/* SELL YOUR MOBILE */}
        <Route path="/sell-your-mobile" element={<SellYourMobile />} />
        <Route path="/verify-email" element={<VerifyEmail />} />

        {/* ABOUT */}
        <Route path="/about" element={<About />} />

        {/* CONTACT */}
        <Route path="/contact" element={<Contact />} />

        {/* ADMIN LOGIN */}
        <Route path="/admin/login" element={<AdminLogin />} />

        {/* ADMIN DASHBOARD */}
        <Route
          path="/admin"
          element={
            <PlaceholderPage title="Admin Dashboard" />
          }
        />

        {/* FALLBACK */}
        <Route
          path="*"
          element={
            <PlaceholderPage title="Page Not Found" />
          }
        />
      </Routes>

      <Footer />
    </>
  );
}

export default App;
