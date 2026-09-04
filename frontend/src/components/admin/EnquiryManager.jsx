import React, { useEffect, useState } from "react";
import API_BASE_URL from "../../config/environment";

const EnquiryManager = () => {
  const [enquiries, setEnquiries] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const token = localStorage.getItem("fixit_admin_token");

  const fetchEnquiries = async () => {
    try {
      setLoading(true);
      setError("");

      const response = await fetch(`${API_BASE_URL}/api/admin/enquiries`, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });

      if (!response.ok) {
        throw new Error("Unable to load enquiries");
      }

      const result = await response.json();
      const data = result?.data ?? result;

      setEnquiries(Array.isArray(data) ? data : []);
    } catch (err) {
      setError(err.message || "Unable to load enquiries");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchEnquiries();
  }, []);

  const whatsappNumber =
    import.meta.env.VITE_WHATSAPP_NUMBER || "";

  const openWhatsApp = (enquiry) => {
    const phone =
      enquiry.phone ||
      enquiry.phoneNumber ||
      "";

    const message = `Hello ${enquiry.name || "Customer"}, this is FIXIT Mobile Sales & Services regarding your enquiry.`;

    const number = phone.replace(/\D/g, "") || whatsappNumber;

    if (!number) {
      alert("No phone number available.");
      return;
    }

    window.open(
      `https://wa.me/${number}?text=${encodeURIComponent(message)}`,
      "_blank"
    );
  };

  return (
    <section className="admin-manager">
      <div className="admin-manager-heading">
        <div>
          <span className="admin-eyebrow">CUSTOMERS</span>
          <h1>Product Enquiries</h1>
          <p>View customer enquiries submitted through the website.</p>
        </div>

        <button
          className="admin-secondary-button"
          onClick={fetchEnquiries}
        >
          Refresh
        </button>
      </div>

      {error && <div className="admin-error">{error}</div>}

      <div className="admin-list-card">
        {loading ? (
          <div className="admin-loading">Loading enquiries...</div>
        ) : enquiries.length === 0 ? (
          <div className="admin-empty">
            <div>💬</div>
            <h3>No enquiries</h3>
            <p>Customer enquiries will appear here.</p>
          </div>
        ) : (
          <div className="admin-table-wrapper">
            <table className="admin-table">
              <thead>
                <tr>
                  <th>Customer</th>
                  <th>Phone</th>
                  <th>Email</th>
                  <th>Product</th>
                  <th>Date</th>
                  <th>Contact</th>
                </tr>
              </thead>

              <tbody>
                {enquiries.map((enquiry) => (
                  <tr key={enquiry.id}>
                    <td>
                      <strong>{enquiry.name || "—"}</strong>
                    </td>

                    <td>
                      {enquiry.phone ||
                        enquiry.phoneNumber ||
                        "—"}
                    </td>

                    <td>{enquiry.email || "—"}</td>

                    <td>
                      {enquiry.productName ||
                        enquiry.product?.name ||
                        "—"}
                    </td>

                    <td>
                      {enquiry.createdAt
                        ? new Date(
                            enquiry.createdAt
                          ).toLocaleDateString("en-IN")
                        : "—"}
                    </td>

                    <td>
                      <button
                        className="admin-whatsapp-button"
                        onClick={() => openWhatsApp(enquiry)}
                      >
                        WhatsApp
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </section>
  );
};

export default EnquiryManager;