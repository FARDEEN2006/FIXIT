import React, { useEffect, useState } from "react";
import { getStoreInfo } from "../services/api";
import "../styles/about.css";
export default function About() { const [store,setStore]=useState(null); useEffect(()=>{getStoreInfo().then(setStore).catch(()=>{});},[]); return <main className="section"><div className="container"><div className="section-header"><span className="section-label">About FIXIT</span><h1>FIXIT Mobile Sales &amp; Services</h1><p>{store?.aboutContent || "The Repair Company."}</p></div></div></main>; }
