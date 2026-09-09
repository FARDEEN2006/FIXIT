import React from "react";
import { Routes, Route, useLocation, useNavigate } from "react-router-dom";
import { ArrowLeft } from "lucide-react";

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

import AdminLayout from "./components/admin/AdminLayout";
import ProtectedRoute from "./components/admin/ProtectedRoute";

function PlaceholderPage({ title }) {
  return (
    <>
      <Navbar />

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

          <p>This page could not be found.</p>
        </div>
      </main>

      <Footer />
    </>
  );
}

function BackButton() {
  const navigate = useNavigate();
  const location = useLocation();

  const goBack = () => {
    if (window.history.length > 1) {
      navigate(-1);
    } else {
      navigate("/");
    }
  };

  return (
    <button
      type="button"
      className="fixit-back-button"
      onClick={goBack}
      aria-label={`Go back from ${location.pathname}`}
      title="Go back"
    >
      <ArrowLeft size={20} aria-hidden="true" />
      <span>Back</span>
    </button>
  );
}

function App() {
  return (
    <>
      <ScrollToTop />
      <BackButton />

      <Routes>
        {/* =========================
            PUBLIC PAGES
        ========================== */}

        <Route
          path="/"
          element={
            <>
              <Navbar />
              <Home />
              <Footer />
            </>
          }
        />

        <Route
          path="/products"
          element={
            <>
              <Navbar />
              <Products />
              <Footer />
            </>
          }
        />

        <Route
          path="/products/:id"
          element={
            <>
              <Navbar />
              <ProductDetails />
              <Footer />
            </>
          }
        />

        <Route
          path="/sell-your-mobile"
          element={
            <>
              <Navbar />
              <SellYourMobile />
              <Footer />
            </>
          }
        />

        <Route
          path="/about"
          element={
            <>
              <Navbar />
              <About />
              <Footer />
            </>
          }
        />

        <Route
          path="/contact"
          element={
            <>
              <Navbar />
              <Contact />
              <Footer />
            </>
          }
        />

        {/* =========================
            ADMIN LOGIN
        ========================== */}

        <Route
          path="/admin/login"
          element={<AdminLogin />}
        />

        {/* =========================
            PROTECTED ADMIN DASHBOARD
        ========================== */}

        <Route element={<ProtectedRoute />}>
          <Route
            path="/admin"
            element={<AdminLayout />}
          />
        </Route>

        {/* =========================
            404
        ========================== */}

        <Route
          path="*"
          element={<PlaceholderPage title="Page Not Found" />}
        />
      </Routes>
    </>
  );
}

export default App;