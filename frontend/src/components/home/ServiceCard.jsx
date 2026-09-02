import React from "react";
import { Wrench } from "lucide-react";

function ServiceCard({ service }) {
  const imageUrl = service?.image_url || service?.imageUrl;

  return (
    <article className="fixit-service-card">

      <div className="fixit-service-icon">

        {imageUrl ? (
          <img
            src={imageUrl}
            alt={service.name}
          />
        ) : (
          <Wrench size={28} />
        )}

      </div>

      <div className="fixit-service-content">

        <h3>
          {service.name}
        </h3>

        {service.description && (
          <p>
            {service.description}
          </p>
        )}

      </div>

    </article>
  );
}

export default ServiceCard;