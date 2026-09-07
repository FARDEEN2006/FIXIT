import { useEffect } from "react";

const businessName = "FIXIT Mobile Sales & Services";
const siteUrl = "https://fixitkarur.in";
const logoUrl = `${siteUrl}/assets/logo/logo.png`;
const description =
  "FIXIT Karur provides mobile sales, phone repair, and reliable mobile services in Karur, Tamil Nadu.";

const businessSchema = {
  "@context": "https://schema.org",
  "@graph": [
    {
      "@type": "LocalBusiness",
      "@id": `${siteUrl}/#business`,
      name: businessName,
      description,
      url: siteUrl,
      logo: logoUrl,
      image: logoUrl,
      telephone: "+918870783647",
      priceRange: "₹₹",
      address: {
        "@type": "PostalAddress",
        streetAddress:
          "Ground Floor, Shop No. 7 & 34, Perris Plaza, Municipality Building, Anna Salai (Backside of the Bus Stand)",
        addressLocality: "Karur",
        addressRegion: "Tamil Nadu",
        postalCode: "639001",
        addressCountry: "IN",
      },
      areaServed: {
        "@type": "City",
        name: "Karur",
      },
      sameAs: [
        "https://www.instagram.com/fixit_the_repair_company",
        "https://youtube.com/@fixit3927",
      ],
    },
    {
      "@type": "WebSite",
      "@id": `${siteUrl}/#website`,
      url: siteUrl,
      name: businessName,
      publisher: {
        "@id": `${siteUrl}/#business`,
      },
    },
  ],
};

export default function SEO({ title, description: pageDescription, path }) {
  useEffect(() => {
    const fullTitle = `${title} | FIXIT Mobile Sales & Services`;
    const canonicalUrl = `${siteUrl}${path === "/" ? "" : path}`;
    const descriptionContent = pageDescription || description;

    document.title = fullTitle;

    const updateMeta = (selector, attribute, value, identity) => {
      let element = document.head.querySelector(selector);
      if (!element) {
        element = document.createElement("meta");
        element.setAttribute(identity.attribute, identity.value);
        document.head.appendChild(element);
      }
      element.setAttribute(attribute, value);
    };

    updateMeta('meta[name="description"]', "content", descriptionContent, {
      attribute: "name",
      value: "description",
    });
    updateMeta('meta[property="og:title"]', "content", fullTitle, {
      attribute: "property",
      value: "og:title",
    });
    updateMeta('meta[property="og:description"]', "content", descriptionContent, {
      attribute: "property",
      value: "og:description",
    });
    updateMeta('meta[property="og:type"]', "content", "website", {
      attribute: "property",
      value: "og:type",
    });
    updateMeta('meta[property="og:site_name"]', "content", businessName, {
      attribute: "property",
      value: "og:site_name",
    });
    updateMeta('meta[property="og:url"]', "content", canonicalUrl, {
      attribute: "property",
      value: "og:url",
    });
    updateMeta('meta[property="og:image"]', "content", logoUrl, {
      attribute: "property",
      value: "og:image",
    });
    updateMeta('meta[name="twitter:card"]', "content", "summary", {
      attribute: "name",
      value: "twitter:card",
    });
    updateMeta('meta[name="twitter:title"]', "content", fullTitle, {
      attribute: "name",
      value: "twitter:title",
    });
    updateMeta('meta[name="twitter:description"]', "content", descriptionContent, {
      attribute: "name",
      value: "twitter:description",
    });
    updateMeta('meta[name="twitter:image"]', "content", logoUrl, {
      attribute: "name",
      value: "twitter:image",
    });

    let canonical = document.head.querySelector('link[rel="canonical"]');
    if (!canonical) {
      canonical = document.createElement("link");
      canonical.setAttribute("rel", "canonical");
      document.head.appendChild(canonical);
    }
    canonical.setAttribute("href", canonicalUrl);

    let schema = document.head.querySelector('script[data-fixit-schema="business"]');
    if (!schema) {
      schema = document.createElement("script");
      schema.type = "application/ld+json";
      schema.dataset.fixitSchema = "business";
      document.head.appendChild(schema);
    }
    schema.textContent = JSON.stringify(businessSchema);
  }, [title, pageDescription, path]);

  return null;
}
