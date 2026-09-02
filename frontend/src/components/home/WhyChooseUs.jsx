import React from "react";
import {
  ShieldCheck,
  Cpu,
  BadgeCheck,
  Headphones,
} from "lucide-react";

function WhyChooseUs() {
  const reasons = [
    {
      icon: ShieldCheck,
      title: "Trusted Service",
      description:
        "Your device is handled with care throughout the service process.",
    },
    {
      icon: Cpu,
      title: "Technical Expertise",
      description:
        "Professional approach to mobile diagnostics and repair.",
    },
    {
      icon: BadgeCheck,
      title: "Quality Focus",
      description:
        "We focus on reliable service and quality workmanship.",
    },
    {
      icon: Headphones,
      title: "Customer Support",
      description:
        "Clear communication and support from enquiry to service.",
    },
  ];

  return (
    <section className="section fixit-why-section">

      <div className="container">

        <div className="section-header">

          <span className="section-label">
            Why FIXIT
          </span>

          <h2 className="section-title">
            Built Around Your Device
          </h2>

          <p className="section-description">
            Professional service, technical care and a
            customer-first approach.
          </p>

        </div>

        <div className="fixit-reasons-grid">

          {reasons.map((reason) => {
            const Icon = reason.icon;

            return (
              <article
                className="fixit-reason-card"
                key={reason.title}
              >

                <div className="fixit-reason-icon">
                  <Icon size={25} />
                </div>

                <h3>
                  {reason.title}
                </h3>

                <p>
                  {reason.description}
                </p>

              </article>
            );
          })}

        </div>

      </div>

    </section>
  );
}

export default WhyChooseUs;