import React, { useEffect, useState } from "react";
import API_BASE_URL from "../../config/environment";

const ListingManager = () => {
  const [listings, setListings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [selectedListing, setSelectedListing] = useState(null);
  const [detailsLoading, setDetailsLoading] = useState(false);

  const token = sessionStorage.getItem("fixit_admin_token");

  const fetchListings = async () => {
    try {
      setLoading(true);
      setError("");

      const response = await fetch(
        `${API_BASE_URL}/api/admin/second-hand`,
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );

      if (!response.ok) {
        throw new Error("Unable to load mobile listings");
      }

      const result = await response.json();

      const listingsData = result?.data?.listings || [];

      setListings(
        Array.isArray(listingsData)
          ? listingsData
          : []
      );
    } catch (err) {
      setError(
        err.message ||
          "Unable to load listings"
      );
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchListings();
  }, []);

  const openListing = async (id) => {
    try {
      setDetailsLoading(true);
      setError("");

      const response = await fetch(
        `${API_BASE_URL}/api/admin/second-hand/${id}`,
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );

      if (!response.ok) {
        throw new Error(
          "Unable to load listing details"
        );
      }

      const result = await response.json();

      const listing = result?.data || null;

      if (!listing) {
        throw new Error(
          "Listing details not found"
        );
      }

      setSelectedListing(listing);
    } catch (err) {
      setError(
        err.message ||
          "Unable to load listing details"
      );
    } finally {
      setDetailsLoading(false);
    }
  };

  const closeListing = () => {
    setSelectedListing(null);
  };

  const updateStatus = async (
    id,
    status
  ) => {
    try {
      const response = await fetch(
        `${API_BASE_URL}/api/admin/second-hand/${id}/status`,
        {
          method: "PUT",
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          },
          body: JSON.stringify({
            status,
          }),
        }
      );

      if (!response.ok) {
        throw new Error(
          "Unable to update status"
        );
      }

      await fetchListings();

      if (
        selectedListing &&
        selectedListing.id === id
      ) {
        setSelectedListing({
          ...selectedListing,
          listingStatus: status,
        });
      }
    } catch (err) {
      setError(
        err.message ||
          "Unable to update status"
      );
    }
  };

  const deleteListing = async (id) => {
    if (
      !window.confirm(
        "Delete this listing permanently?"
      )
    ) {
      return;
    }

    try {
      const response = await fetch(
        `${API_BASE_URL}/api/admin/second-hand/${id}`,
        {
          method: "DELETE",
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );

      if (!response.ok) {
        throw new Error(
          "Unable to delete listing"
        );
      }

      if (
        selectedListing &&
        selectedListing.id === id
      ) {
        setSelectedListing(null);
      }

      await fetchListings();
    } catch (err) {
      setError(
        err.message ||
          "Unable to delete listing"
      );
    }
  };

  const getThumbnail = (listing) => {
    if (
      !Array.isArray(listing?.images) ||
      listing.images.length === 0
    ) {
      return null;
    }

    return (
      listing.images.find(
        (image) => image.isThumbnail
      ) ||
      listing.images[0]
    );
  };

  return (
    <section className="admin-manager">
      <div className="admin-manager-heading">
        <div>
          <span className="admin-eyebrow">
            SECOND-HAND
          </span>

          <h1>Sell Your Mobile</h1>

          <p>
            Review private mobile-selling
            submissions.
          </p>
        </div>

        <button
          className="admin-secondary-button"
          onClick={fetchListings}
        >
          Refresh
        </button>
      </div>

      {error && (
        <div className="admin-error">
          {error}
        </div>
      )}

      <div className="admin-list-card">
        {loading ? (
          <div className="admin-loading">
            Loading listings...
          </div>
        ) : listings.length === 0 ? (
          <div className="admin-empty">
            <div>📦</div>

            <h3>
              No mobile listings
            </h3>

            <p>
              New private submissions
              will appear here.
            </p>
          </div>
        ) : (
          <div
            className="admin-table-wrapper"
            style={{
              overflowX: "auto",
            }}
          >
            <table className="admin-table">
              <thead>
                <tr>
                  <th>Image</th>
                  <th>Mobile</th>
                  <th>Seller</th>
                  <th>Phone</th>
                  <th>Expected Price</th>
                  <th>Status</th>
                  <th>Action</th>
                </tr>
              </thead>

              <tbody>
                {listings.map((listing) => {
                  const thumbnail =
                    getThumbnail(listing);

                  return (
                    <tr
                      key={listing.id}
                      onClick={() =>
                        openListing(
                          listing.id
                        )
                      }
                      style={{
                        cursor: "pointer",
                      }}
                      title="Click to view complete listing"
                    >
                      <td
                        onClick={(event) =>
                          event.stopPropagation()
                        }
                      >
                        {thumbnail?.imageUrl ? (
                          <img
                            src={
                              thumbnail.imageUrl
                            }
                            alt={
                              listing.productName ||
                              "Mobile"
                            }
                            style={{
                              width: "72px",
                              height: "72px",
                              objectFit:
                                "cover",
                              borderRadius:
                                "10px",
                              border:
                                "1px solid #e1e7eb",
                              display: "block",
                              background:
                                "#f0f3f5",
                            }}
                          />
                        ) : (
                          <div
                            style={{
                              width: "72px",
                              height: "72px",
                              borderRadius:
                                "10px",
                              background:
                                "#f0f3f5",
                              border:
                                "1px solid #e1e7eb",
                              display: "flex",
                              alignItems:
                                "center",
                              justifyContent:
                                "center",
                              fontSize: "24px",
                            }}
                          >
                            📱
                          </div>
                        )}
                      </td>

                      <td>
                        <strong>
                          {listing.productName ||
                            "Mobile"}
                        </strong>
                      </td>

                      <td>
                        {listing.sellerName ||
                          "—"}
                      </td>

                      <td>
                        {listing.sellerPhone ||
                          "—"}
                      </td>

                      <td>
                        ₹
                        {Number(
                          listing.expectedPrice ||
                            0
                        ).toLocaleString(
                          "en-IN"
                        )}
                      </td>

                      <td
                        onClick={(event) =>
                          event.stopPropagation()
                        }
                      >
                        <select
                          value={
                            listing.listingStatus ||
                            "NEW"
                          }
                          onChange={(
                            event
                          ) =>
                            updateStatus(
                              listing.id,
                              event.target
                                .value
                            )
                          }
                          className="admin-status-select"
                        >
                          <option value="NEW">
                            NEW
                          </option>

                          <option value="REVIEWING">
                            REVIEWING
                          </option>

                          <option value="CONTACTED">
                            CONTACTED
                          </option>

                          <option value="ACCEPTED">
                            ACCEPTED
                          </option>

                          <option value="REJECTED">
                            REJECTED
                          </option>

                          <option value="COMPLETED">
                            COMPLETED
                          </option>
                        </select>
                      </td>

                      <td
                        onClick={(event) =>
                          event.stopPropagation()
                        }
                      >
                        <button
                          className="admin-danger-button"
                          onClick={() =>
                            deleteListing(
                              listing.id
                            )
                          }
                        >
                          Delete
                        </button>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {detailsLoading && (
        <div
          style={{
            position: "fixed",
            inset: 0,
            background:
              "rgba(15, 23, 42, 0.35)",
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            zIndex: 9999,
          }}
        >
          <div
            style={{
              background: "#ffffff",
              padding: "24px 32px",
              borderRadius: "12px",
              fontWeight: 600,
            }}
          >
            Loading listing...
          </div>
        </div>
      )}

      {selectedListing && (
        <div
          onClick={closeListing}
          style={{
            position: "fixed",
            inset: 0,
            background:
              "rgba(15, 23, 42, 0.55)",
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            padding: "24px",
            zIndex: 10000,
            overflowY: "auto",
          }}
        >
          <div
            onClick={(event) =>
              event.stopPropagation()
            }
            style={{
              width: "min(100%, 1000px)",
              maxHeight: "92vh",
              overflowY: "auto",
              background: "#ffffff",
              borderRadius: "18px",
              padding: "28px",
              boxShadow:
                "0 24px 70px rgba(0,0,0,0.22)",
            }}
          >
            <div
              style={{
                display: "flex",
                justifyContent:
                  "space-between",
                alignItems: "flex-start",
                gap: "20px",
                marginBottom: "24px",
              }}
            >
              <div>
                <span
                  className="admin-eyebrow"
                >
                  MOBILE LISTING
                </span>

                <h2
                  style={{
                    margin:
                      "6px 0 4px",
                    color: "#1b263b",
                  }}
                >
                  {selectedListing.productName ||
                    "Mobile"}
                </h2>

                <p
                  style={{
                    margin: 0,
                    color: "#64748b",
                  }}
                >
                  Complete seller
                  submission details
                </p>
              </div>

              <button
                onClick={closeListing}
                style={{
                  width: "40px",
                  height: "40px",
                  border: "none",
                  borderRadius:
                    "50%",
                  background:
                    "#f0f3f5",
                  fontSize: "22px",
                  cursor: "pointer",
                }}
                aria-label="Close"
              >
                ×
              </button>
            </div>

            <div
              style={{
                display: "grid",
                gridTemplateColumns:
                  "minmax(0, 1.25fr) minmax(280px, 0.75fr)",
                gap: "28px",
              }}
            >
              <div>
                <h3
                  style={{
                    margin:
                      "0 0 14px",
                    color: "#1b263b",
                  }}
                >
                  Uploaded Images
                </h3>

                {Array.isArray(
                  selectedListing.images
                ) &&
                selectedListing.images.length >
                  0 ? (
                  <div
                    style={{
                      display: "grid",
                      gridTemplateColumns:
                        "repeat(2, minmax(0, 1fr))",
                      gap: "14px",
                    }}
                  >
                    {selectedListing.images
                      .slice(0, 4)
                      .map((image, index) => (
                        <div
                          key={
                            image.id ||
                            index
                          }
                          style={{
                            position:
                              "relative",
                            aspectRatio:
                              "4 / 3",
                            overflow:
                              "hidden",
                            borderRadius:
                              "12px",
                            background:
                              "#f0f3f5",
                            border:
                              "1px solid #e1e7eb",
                          }}
                        >
                          <img
                            src={
                              image.imageUrl
                            }
                            alt={`Mobile view ${
                              index + 1
                            }`}
                            style={{
                              width: "100%",
                              height:
                                "100%",
                              objectFit:
                                "cover",
                              display:
                                "block",
                            }}
                          />

                          {image.isThumbnail && (
                            <span
                              style={{
                                position:
                                  "absolute",
                                top: "10px",
                                left: "10px",
                                padding:
                                  "5px 9px",
                                borderRadius:
                                  "6px",
                                background:
                                  "#00d0d0",
                                color:
                                  "#1b263b",
                                fontSize:
                                  "11px",
                                fontWeight:
                                  700,
                              }}
                            >
                              THUMBNAIL
                            </span>
                          )}
                        </div>
                      ))}
                  </div>
                ) : (
                  <div
                    style={{
                      padding:
                        "50px 20px",
                      textAlign:
                        "center",
                      background:
                        "#f0f3f5",
                      borderRadius:
                        "12px",
                      color:
                        "#64748b",
                    }}
                  >
                    No images uploaded.
                  </div>
                )}
              </div>

              <div>
                <h3
                  style={{
                    margin:
                      "0 0 16px",
                    color: "#1b263b",
                  }}
                >
                  Listing Details
                </h3>

                <div
                  style={{
                    display: "grid",
                    gap: "14px",
                  }}
                >
                  <div>
                    <small
                      style={{
                        color:
                          "#64748b",
                      }}
                    >
                      Seller
                    </small>

                    <div
                      style={{
                        fontWeight: 600,
                      }}
                    >
                      {selectedListing.sellerName ||
                        "—"}
                    </div>
                  </div>

                  <div>
                    <small
                      style={{
                        color:
                          "#64748b",
                      }}
                    >
                      Phone
                    </small>

                    <div
                      style={{
                        fontWeight: 600,
                      }}
                    >
                      {selectedListing.sellerPhone ||
                        "—"}
                    </div>
                  </div>

                  <div>
                    <small
                      style={{
                        color:
                          "#64748b",
                      }}
                    >
                      Email
                    </small>

                    <div
                      style={{
                        fontWeight: 600,
                        wordBreak:
                          "break-word",
                      }}
                    >
                      {selectedListing.sellerEmail ||
                        "—"}
                    </div>
                  </div>

                  <div>
                    <small
                      style={{
                        color:
                          "#64748b",
                      }}
                    >
                      Condition
                    </small>

                    <div
                      style={{
                        fontWeight: 600,
                      }}
                    >
                      {selectedListing.condition ||
                        "—"}
                    </div>
                  </div>

                  <div>
                    <small
                      style={{
                        color:
                          "#64748b",
                      }}
                    >
                      Expected Price
                    </small>

                    <div
                      style={{
                        fontWeight: 700,
                        fontSize:
                          "20px",
                        color:
                          "#1b263b",
                      }}
                    >
                      ₹
                      {Number(
                        selectedListing.expectedPrice ||
                          0
                      ).toLocaleString(
                        "en-IN"
                      )}
                    </div>
                  </div>

                  <div>
                    <small
                      style={{
                        color:
                          "#64748b",
                      }}
                    >
                      Status
                    </small>

                    <div
                      style={{
                        marginTop:
                          "6px",
                      }}
                    >
                      <select
                        value={
                          selectedListing.listingStatus ||
                          "NEW"
                        }
                        onChange={(
                          event
                        ) =>
                          updateStatus(
                            selectedListing.id,
                            event.target
                              .value
                          )
                        }
                        className="admin-status-select"
                      >
                        <option value="NEW">
                          NEW
                        </option>

                        <option value="REVIEWING">
                          REVIEWING
                        </option>

                        <option value="CONTACTED">
                          CONTACTED
                        </option>

                        <option value="ACCEPTED">
                          ACCEPTED
                        </option>

                        <option value="REJECTED">
                          REJECTED
                        </option>

                        <option value="COMPLETED">
                          COMPLETED
                        </option>
                      </select>
                    </div>
                  </div>

                  <div>
                    <small
                      style={{
                        color:
                          "#64748b",
                      }}
                    >
                      Description
                    </small>

                    <div
                      style={{
                        marginTop:
                          "5px",
                        lineHeight:
                          1.6,
                        whiteSpace:
                          "pre-wrap",
                      }}
                    >
                      {selectedListing.detailedDescription ||
                        "No description provided."}
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div
              style={{
                marginTop: "28px",
                paddingTop: "20px",
                borderTop:
                  "1px solid #e1e7eb",
                display: "flex",
                justifyContent:
                  "flex-end",
                gap: "10px",
              }}
            >
              <button
                className="admin-danger-button"
                onClick={() =>
                  deleteListing(
                    selectedListing.id
                  )
                }
              >
                Delete Listing
              </button>

              <button
                className="admin-secondary-button"
                onClick={closeListing}
              >
                Close
              </button>
            </div>
          </div>
        </div>
      )}
    </section>
  );
};

export default ListingManager;