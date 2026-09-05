import React, { useState } from "react";
import { adminLogin } from "../services/api";

export default function AdminLogin() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");

  const submit = async (event) => {
    event.preventDefault();
    setError("");

    try {
      const response = await adminLogin(email, password);
      sessionStorage.setItem("fixit_admin_token", response.data.token);
      window.location.assign("/admin");
    } catch (err) {
      setError(err.message || "Unable to sign in.");
    }
  };

  return (
    <main className="section">
      <div className="container">
        <form className="fixit-sell-form" onSubmit={submit}>
          <h1>Admin sign in</h1>
          <label>
            Email
            <input
              required
              type="email"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
            />
          </label>
          <label>
            Password
            <input
              required
              type="password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
            />
          </label>
          {error && <p className="fixit-form-error">{error}</p>}
          <button className="fixit-button fixit-button-primary">
            Sign in
          </button>
        </form>
      </div>
    </main>
  );
}
