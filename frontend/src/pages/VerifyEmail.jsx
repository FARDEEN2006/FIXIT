import React, { useEffect, useState } from "react";
import { useSearchParams } from "react-router-dom";
import { confirmListingVerification, uploadListingImage } from "../services/api";

export default function VerifyEmail() {
  const [params] = useSearchParams(); const listingId = params.get("listing"); const token = params.get("token");
  const [state, setState] = useState("Verifying your email…"); const [verified, setVerified] = useState(false); const [files, setFiles] = useState([]); const [error, setError] = useState("");
  useEffect(() => { if (!listingId || !token) { setState("This verification link is incomplete."); return; } confirmListingVerification(listingId, token).then(() => { setVerified(true); setState("Email verified. You may now upload up to four photos."); }).catch(e => setState(e.message || "This verification link is invalid or expired.")); }, [listingId, token]);
  const upload = async e => { e.preventDefault(); setError(""); try { for (let i=0;i<files.length;i++) await uploadListingImage(listingId, token, files[i], i===0); setState("Your photos were uploaded successfully. FIXIT will review your private listing."); setFiles([]); setVerified(false); } catch (err) { setError(err.message || "Image upload failed."); } };
  return <main className="section"><div className="container"><div className="section-header"><span className="section-label">Email verification</span><h1>Sell your mobile</h1><p role="status">{state}</p>{verified && <form className="fixit-sell-form" onSubmit={upload}><label>Device photos (up to 4)<input required type="file" accept="image/jpeg,image/png,image/webp" multiple onChange={e=>setFiles(Array.from(e.target.files).slice(0,4))}/></label><p>{files.length} image(s) selected. The first image is the thumbnail.</p>{error && <p className="fixit-form-error">{error}</p>}<button className="fixit-button fixit-button-primary">Upload private photos</button></form>}</div></div></main>;
}
