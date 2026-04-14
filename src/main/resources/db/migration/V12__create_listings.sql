CREATE TABLE listings (
                          id UUID PRIMARY KEY,
                          worker_id UUID NOT NULL REFERENCES worker_profiles(id),
                          title VARCHAR(200) NOT NULL,
                          description TEXT,
                          price INTEGER NOT NULL,
                          price_unit VARCHAR(100),
                          category VARCHAR(100),
                          photos TEXT[],
                          active BOOLEAN NOT NULL DEFAULT TRUE,
                          created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                          updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_listings_worker_id ON listings(worker_id);
CREATE INDEX idx_listings_active ON listings(active);
CREATE INDEX idx_listings_category ON listings(category);