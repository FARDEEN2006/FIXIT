import React, { useEffect, useState } from "react";
import { getStoreInfo } from "../services/api";
import "../styles/contact.css";
export default function Contact() { const [store,setStore]=useState(null); useEffect(()=>{getStoreInfo().then(setStore).catch(()=>{});},[]); return <main className="section"><div className="container"><div className="section-header"><span className="section-label">Contact</span><h1>Talk to FIXIT</h1>{store ? <address><p>{store.address}, {store.city}, {store.state} {store.pincode}</p><p><a href={`tel:${store.phoneNumber}`}>{store.phoneNumber}</a> · <a href={`mailto:${store.email}`}>{store.email}</a></p></address> : <p>Store contact information will be available soon.</p>}</div></div></main>; }
