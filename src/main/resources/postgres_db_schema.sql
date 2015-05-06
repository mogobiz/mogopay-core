--
-- PostgreSQL database dump
--

SET statement_timeout = 0;
SET lock_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

--
-- Name: plpgsql; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;


--
-- Name: EXTENSION plpgsql; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';


SET search_path = public, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: account; Type: TABLE; Schema: public; Owner: mogobiz; Tablespace: 
--

CREATE TABLE account (
    id bigint NOT NULL,
    account_type character varying(255),
    active boolean NOT NULL,
    autosign boolean NOT NULL,
    birthdate timestamp without time zone,
    civility character varying(255),
    company_fk bigint,
    date_created timestamp without time zone NOT NULL,
    email character varying(255) NOT NULL,
    external_id character varying(255),
    first_name character varying(255),
    last_name character varying(255),
    last_updated timestamp without time zone NOT NULL,
    location_fk bigint,
    login character varying(255) NOT NULL,
    password character varying(255) NOT NULL,
    phone character varying(255),
    token character varying(255),
    token_secret character varying(255),
    uuid character varying(255) NOT NULL
);


ALTER TABLE public.account OWNER TO mogobiz;

--
-- Name: account_xrole; Type: TABLE; Schema: public; Owner: mogobiz; Tablespace: 
--

CREATE TABLE account_xrole (
    roles_fk bigint,
    role_id bigint
);


ALTER TABLE public.account_xrole OWNER TO mogobiz;

--
-- Name: album; Type: TABLE; Schema: public; Owner: mogobiz; Tablespace: 
--

CREATE TABLE album (
    id bigint NOT NULL,
    company_fk bigint,
    date_created timestamp without time zone NOT NULL,
    description character varying(255),
    last_updated timestamp without time zone NOT NULL,
    name character varying(255) NOT NULL,
    uuid character varying(255) NOT NULL
);


ALTER TABLE public.album OWNER TO mogobiz;

--
-- Name: b_o_account; Type: TABLE; Schema: public; Owner: mogobiz; Tablespace: 
--

CREATE TABLE b_o_account (
    id bigint NOT NULL,
    company character varying(255),
    date_created timestamp without time zone NOT NULL,
    email character varying(255) NOT NULL,
    extra text,
    last_updated timestamp without time zone NOT NULL,
    uuid character varying(255) NOT NULL
);


ALTER TABLE public.b_o_account OWNER TO mogobiz;

--
-- Name: b_o_cart; Type: TABLE; Schema: public; Owner: mogobiz; Tablespace: 
--

CREATE TABLE b_o_cart (
    id bigint NOT NULL,
    buyer character varying(255) NOT NULL,
    company_fk bigint NOT NULL,
    currency_code character varying(255) NOT NULL,
    currency_rate double precision NOT NULL,
    xdate timestamp without time zone NOT NULL,
    date_created timestamp without time zone NOT NULL,
    last_updated timestamp without time zone NOT NULL,
    price bigint NOT NULL,
    status character varying(255) NOT NULL,
    transaction_uuid character varying(255) NOT NULL,
    uuid character varying(255) NOT NULL
);


ALTER TABLE public.b_o_cart OWNER TO mogobiz;

--
-- Name: b_o_cart_item; Type: TABLE; Schema: public; Owner: mogobiz; Tablespace: 
--

CREATE TABLE b_o_cart_item (
    id bigint NOT NULL,
    b_o_cart_fk bigint NOT NULL,
    b_o_delivery_fk bigint,
    code character varying(255) NOT NULL,
    date_created timestamp without time zone NOT NULL,
    end_date timestamp without time zone,
    end_price bigint NOT NULL,
    hidden boolean NOT NULL,
    last_updated timestamp without time zone NOT NULL,
    price bigint NOT NULL,
    quantity integer NOT NULL,
    start_date timestamp without time zone,
    tax real NOT NULL,
    ticket_type_fk bigint NOT NULL,
    total_end_price bigint NOT NULL,
    total_price bigint NOT NULL,
    uuid character varying(255) NOT NULL
);


ALTER TABLE public.b_o_cart_item OWNER TO mogobiz;

--
-- Name: b_o_cart_item_b_o_product; Type: TABLE; Schema: public; Owner: mogobiz; Tablespace: 
--

CREATE TABLE b_o_cart_item_b_o_product (
    b_o_products_fk bigint,
    boproduct_id bigint
);


ALTER TABLE public.b_o_cart_item_b_o_product OWNER TO mogobiz;

--
-- Name: b_o_delivery; Type: TABLE; Schema: public; Owner: mogobiz; Tablespace: 
--

CREATE TABLE b_o_delivery (
    id bigint NOT NULL,
    b_o_cart_fk bigint NOT NULL,
    date_created timestamp without time zone NOT NULL,
    extra text,
    last_updated timestamp without time zone NOT NULL,
    status character varying(255) NOT NULL,
    tracking character varying(255),
    uuid character varying(255) NOT NULL
);


ALTER TABLE public.b_o_delivery OWNER TO mogobiz;

--
-- Name: b_o_product; Type: TABLE; Schema: public; Owner: mogobiz; Tablespace: 
--

CREATE TABLE b_o_product (
    id bigint NOT NULL,
    acquittement boolean NOT NULL,
    date_created timestamp without time zone NOT NULL,
    last_updated timestamp without time zone NOT NULL,
    price bigint NOT NULL,
    principal boolean NOT NULL,
    product_fk bigint,
    uuid character varying(255) NOT NULL
);


ALTER TABLE public.b_o_product OWNER TO mogobiz;

--
-- Name: b_o_product_consumption; Type: TABLE; Schema: public; Owner: mogobiz; Tablespace: 
--

CREATE TABLE b_o_product_consumption (
    consumptions_fk bigint,
    consumption_id bigint
);


ALTER TABLE public.b_o_product_consumption OWNER TO mogobiz;

--
-- Name: b_o_return; Type: TABLE; Schema: public; Owner: mogobiz; Tablespace: 
--

CREATE TABLE b_o_return (
    id bigint NOT NULL,
    b_o_cart_fk bigint NOT NULL,
    date_created timestamp without time zone NOT NULL,
    last_updated timestamp without time zone NOT NULL,
    motivation character varying(255),
    status character varying(255) NOT NULL,
    uuid character varying(255) NOT NULL
);


ALTER TABLE public.b_o_return OWNER TO mogobiz;

--
-- Name: b_o_returned_item; Type: TABLE; Schema: public; Owner: mogobiz; Tablespace: 
--

CREATE TABLE b_o_returned_item (
    id bigint NOT NULL,
    b_o_cart_item_fk bigint,
    b_o_return_fk bigint,
    date_created timestamp without time zone NOT NULL,
    last_updated timestamp without time zone NOT NULL,
    quantity integer NOT NULL,
    refunded bigint NOT NULL,
    status character varying(255) NOT NULL,
    total_refunded bigint NOT NULL,
    uuid character varying(255) NOT NULL
);


ALTER TABLE public.b_o_returned_item OWNER TO mogobiz;

--
-- Name: b_o_ticket_type; Type: TABLE; Schema: public; Owner: mogobiz; Tablespace: 
--

CREATE TABLE b_o_ticket_type (
    id bigint NOT NULL,
    age integer NOT NULL,
    b_o_product_fk bigint NOT NULL,
    birthdate timestamp without time zone,
    date_created timestamp without time zone NOT NULL,
    email character varying(255),
    end_date timestamp without time zone,
    firstname character varying(255),
    last_updated timestamp without time zone NOT NULL,
    lastname character varying(255),
    phone character varying(255),
    price bigint NOT NULL,
    qrcode text,
    qrcode_content text,
    quantity integer NOT NULL,
    short_code character varying(255),
    start_date timestamp without time zone,
    ticket_type character varying(255),
    uuid character varying(255) NOT NULL
);


ALTER TABLE public.b_o_ticket_type OWNER TO mogobiz;

--
-- Name: b_o_transaction; Type: TABLE; Schema: public; Owner: mogobiz; Tablespace: 
--

CREATE TABLE b_o_transaction (
    id bigint NOT NULL,
    date_created timestamp without time zone NOT NULL,
    extra text,
    last_updated timestamp without time zone NOT NULL,
    uuid character varying(255) NOT NULL
);


ALTER TABLE public.b_o_transaction OWNER TO mogobiz;

--
-- Name: brand; Type: TABLE; Schema: public; Owner: mogobiz; Tablespace: 
--

CREATE TABLE brand (
    id bigint NOT NULL,
    company_fk bigint NOT NULL,
    date_created timestamp without time zone NOT NULL,
    description text,
    facebooksite character varying(255),
    hide boolean NOT NULL,
    ibeacon_fk bigint,
    last_updated timestamp without time zone NOT NULL,
    name character varying(255) NOT NULL,
    parent_fk bigint,
    twitter character varying(255),
    uuid character varying(255) NOT NULL,
    website character varying(255)
);


ALTER TABLE public.brand OWNER TO mogobiz;

--
-- Name: brand_property; Type: TABLE; Schema: public; Owner: mogobiz; Tablespace: 
--

CREATE TABLE brand_property (
    id bigint NOT NULL,
    brand_fk bigint NOT NULL,
    date_created timestamp without time zone NOT NULL,
    last_updated timestamp without time zone NOT NULL,
    name character varying(255) NOT NULL,
    uuid character varying(255) NOT NULL,
    value character varying(255)
);


ALTER TABLE public.brand_property OWNER TO mogobiz;

--
-- Name: category; Type: TABLE; Schema: public; Owner: mogobiz; Tablespace: 
--

CREATE TABLE category (
    id bigint NOT NULL,
    catalog_fk bigint,
    company_fk bigint NOT NULL,
    date_created timestamp without time zone NOT NULL,
    deleted boolean NOT NULL,
    description text,
    external_code character varying(255),
    google_category character varying(255),
    hide boolean NOT NULL,
    ibeacon_fk bigint,
    keywords character varying(255),
    last_updated timestamp without time zone NOT NULL,
    name character varying(255) NOT NULL,
    parent_fk bigint,
    "position" integer NOT NULL,
    return_max_delay bigint NOT NULL,
    sanitized_name character varying(255),
    uuid character varying(255) NOT NULL
);


ALTER TABLE public.category OWNER TO mogobiz;

--
-- Name: company; Type: TABLE; Schema: public; Owner: mogobiz; Tablespace: 
--

CREATE TABLE company (
    id bigint NOT NULL,
    aes_password character varying(255) NOT NULL,
    api_key character varying(255),
    code character varying(255) NOT NULL,
    country_code character varying(255),
    currency_code character varying(255),
    date_created timestamp without time zone NOT NULL,
    default_language character varying(255) NOT NULL,
    email character varying(255),
    external_code character varying(255),
    gakey character varying(255),
    google_content_fk bigint,
    google_env_fk bigint,
    handling_time integer,
    last_updated timestamp without time zone NOT NULL,
    location_fk bigint,
    map_provider character varying(255),
    name character varying(255) NOT NULL,
    online_validation boolean NOT NULL,
    phone character varying(255),
    refund_policy character varying(255),
    return_policy integer,
    ship_from_fk bigint,
    shipping_carriers_fedex boolean,
    shipping_carriers_ups boolean,
    shipping_international boolean NOT NULL,
    start_date timestamp without time zone,
    stop_date timestamp without time zone,
    temp_session_id character varying(255),
    uuid character varying(255) NOT NULL,
    warehouse_fk bigint,
    website character varying(255),
    weight_unit character varying(255)
);


ALTER TABLE public.company OWNER TO mogobiz;

--
-- Name: company_property; Type: TABLE; Schema: public; Owner: mogobiz; Tablespace: 
--

CREATE TABLE company_property (
    id bigint NOT NULL,
    company_fk bigint NOT NULL,
    date_created timestamp without time zone NOT NULL,
    last_updated timestamp without time zone NOT NULL,
    name character varying(255) NOT NULL,
    uuid character varying(255) NOT NULL,
    value character varying(255) NOT NULL
);


ALTER TABLE public.company_property OWNER TO mogobiz;

--
-- Name: consumption; Type: TABLE; Schema: public; Owner: mogobiz; Tablespace: 
--

CREATE TABLE consumption (
    id bigint NOT NULL,
    b_o_ticket_type_fk bigint,
    xdate timestamp without time zone NOT NULL,
    date_created timestamp without time zone NOT NULL,
    last_updated timestamp without time zone NOT NULL,
    uuid character varying(255) NOT NULL
);


ALTER TABLE public.consumption OWNER TO mogobiz;

--
-- Name: country; Type: TABLE; Schema: public; Owner: mogobiz; Tablespace: 
--

CREATE TABLE country (
    code character varying(255) NOT NULL,
    billing boolean NOT NULL,
    currency_code character varying(255),
    currency_name character varying(255),
    currency_numeric_code character varying(255),
    date_created timestamp without time zone NOT NULL,
    last_updated timestamp without time zone NOT NULL,
    name character varying(255) NOT NULL,
    phone_code character varying(255),
    postal_code_regex character varying(255),
    shipping boolean NOT NULL,
    uuid character varying(255) NOT NULL
);


ALTER TABLE public.country OWNER TO mogobiz;

--
-- Name: country_admin; Type: TABLE; Schema: public; Owner: mogobiz; Tablespace: 
--

CREATE TABLE country_admin (
    id bigint NOT NULL,
    code character varying(255),
    country_fk character varying(255) NOT NULL,
    date_created timestamp without time zone NOT NULL,
    last_updated timestamp without time zone NOT NULL,
    xlevel integer NOT NULL,
    name character varying(255),
    parent_fk bigint,
    uuid character varying(255) NOT NULL
);


ALTER TABLE public.country_admin OWNER TO mogobiz;

--
-- Name: coupon; Type: TABLE; Schema: public; Owner: mogobiz; Tablespace: 
--

CREATE TABLE coupon (
    id bigint NOT NULL,
    active boolean NOT NULL,
    anonymous boolean NOT NULL,
    catalog_wise boolean NOT NULL,
    code character varying(255) NOT NULL,
    company_fk bigint NOT NULL,
    consumed bigint NOT NULL,
    date_created timestamp without time zone NOT NULL,
    description text,
    end_date timestamp without time zone,
    for_sale boolean NOT NULL,
    last_updated timestamp without time zone NOT NULL,
    name character varying(255) NOT NULL,
    number_of_uses bigint,
    pastille character varying(255),
    start_date timestamp without time zone,
    uuid character varying(255) NOT NULL
);


ALTER TABLE public.coupon OWNER TO mogobiz;

--
-- Name: coupon_category; Type: TABLE; Schema: public; Owner: mogobiz; Tablespace: 
--

CREATE TABLE coupon_category (
    categories_fk bigint,
    category_id bigint
);


ALTER TABLE public.coupon_category OWNER TO mogobiz;

--
-- Name: coupon_product; Type: TABLE; Schema: public; Owner: mogobiz; Tablespace: 
--

CREATE TABLE coupon_product (
    products_fk bigint,
    product_id bigint
);


ALTER TABLE public.coupon_product OWNER TO mogobiz;

--
-- Name: coupon_reduction_rule; Type: TABLE; Schema: public; Owner: mogobiz; Tablespace: 
--

CREATE TABLE coupon_reduction_rule (
    rules_fk bigint,
    reduction_rule_id bigint
);


ALTER TABLE public.coupon_reduction_rule OWNER TO mogobiz;

--
-- Name: coupon_ticket_type; Type: TABLE; Schema: public; Owner: mogobiz; Tablespace: 
--

CREATE TABLE coupon_ticket_type (
    ticket_types_fk bigint,
    ticket_type_id bigint
);


ALTER TABLE public.coupon_ticket_type OWNER TO mogobiz;

--
-- Name: customer_profile; Type: TABLE; Schema: public; Owner: mogobiz; Tablespace: 
--

CREATE TABLE customer_profile (
    id bigint NOT NULL,
    active boolean NOT NULL,
    code character varying(255) NOT NULL,
    date_created timestamp without time zone NOT NULL,
    description text NOT NULL,
    last_updated timestamp without time zone NOT NULL,
    name character varying(255) NOT NULL,
    uuid character varying(255) NOT NULL
);


ALTER TABLE public.customer_profile OWNER TO mogobiz;

--
-- Name: date_period; Type: TABLE; Schema: public; Owner: mogobiz; Tablespace: 
--

CREATE TABLE date_period (
    id bigint NOT NULL,
    date_created timestamp without time zone NOT NULL,
    end_date timestamp without time zone NOT NULL,
    last_updated timestamp without time zone NOT NULL,
    product_fk bigint,
    start_date timestamp without time zone NOT NULL,
    uuid character varying(255) NOT NULL
);


ALTER TABLE public.date_period OWNER TO mogobiz;

--
-- Name: es_env; Type: TABLE; Schema: public; Owner: mogobiz; Tablespace: 
--

CREATE TABLE es_env (
    id bigint NOT NULL,
    active boolean NOT NULL,
    company_fk bigint NOT NULL,
    cron_expr character varying(255) NOT NULL,
    date_created timestamp without time zone NOT NULL,
    extra text,
    idx character varying(255),
    last_updated timestamp without time zone NOT NULL,
    name character varying(255) NOT NULL,
    running boolean NOT NULL,
    success boolean NOT NULL,
    url character varying(255) NOT NULL,
    uuid character varying(255) NOT NULL
);


ALTER TABLE public.es_env OWNER TO mogobiz;

--
-- Name: event_period_sale; Type: TABLE; Schema: public; Owner: mogobiz; Tablespace: 
--

CREATE TABLE event_period_sale (
    id bigint NOT NULL,
    date_created timestamp without time zone NOT NULL,
    event_date timestamp without time zone,
    event_start_time timestamp without time zone,
    last_updated timestamp without time zone NOT NULL,
    nb_ticket_sold bigint NOT NULL,
    product_fk bigint,
    uuid character varying(255) NOT NULL
);


ALTER TABLE public.event_period_sale OWNER TO mogobiz;

--
-- Name: external_account; Type: TABLE; Schema: public; Owner: mogobiz; Tablespace: 
--

CREATE TABLE external_account (
    id bigint NOT NULL,
    account_type character varying(255) NOT NULL,
    date_created timestamp without time zone NOT NULL,
    external_id character varying(255),
    last_updated timestamp without time zone NOT NULL,
    login character varying(255) NOT NULL,
    token character varying(255),
    token_secret character varying(255),
    user_fk bigint NOT NULL,
    uuid character varying(255) NOT NULL
);


ALTER TABLE public.external_account OWNER TO mogobiz;

--
-- Name: external_auth_login; Type: TABLE; Schema: public; Owner: mogobiz; Tablespace: 
--

CREATE TABLE external_auth_login (
    id bigint NOT NULL,
    account_type character varying(255) NOT NULL,
    birth_date character varying(255),
    city character varying(255),
    country character varying(255),
    date_created timestamp without time zone NOT NULL,
    email character varying(255),
    external_id character varying(255),
    first_name character varying(255),
    gender character varying(255),
    instant timestamp without time zone,
    last_name character varying(255),
    last_updated timestamp without time zone NOT NULL,
    login character varying(255) NOT NULL,
    mobile character varying(255),
    postal_code character varying(255),
    road1 character varying(255),
    road2 character varying(255),
    road3 character varying(255),
    road4 character varying(255),
    token character varying(255),
    token_secret character varying(255),
    uuid character varying(255) NOT NULL
);


ALTER TABLE public.external_auth_login OWNER TO mogobiz;

--
-- Name: feature; Type: TABLE; Schema: public; Owner: mogobiz; Tablespace: 
--

CREATE TABLE feature (
    id bigint NOT NULL,
    category_fk bigint,
    date_created timestamp without time zone NOT NULL,
    domain character varying(255),
    external_code character varying(255),
    hide boolean NOT NULL,
    last_updated timestamp without time zone NOT NULL,
    name character varying(255) NOT NULL,
    "position" integer NOT NULL,
    product_fk bigint,
    uuid character varying(255) NOT NULL,
    value character varying(255)
);


ALTER TABLE public.feature OWNER TO mogobiz;

--
-- Name: feature_value; Type: TABLE; Schema: public; Owner: mogobiz; Tablespace: 
--

CREATE TABLE feature_value (
    id bigint NOT NULL,
    date_created timestamp without time zone NOT NULL,
    feature_fk bigint NOT NULL,
    last_updated timestamp without time zone NOT NULL,
    product_fk bigint NOT NULL,
    uuid character varying(255) NOT NULL,
    value character varying(255)
);


ALTER TABLE public.feature_value OWNER TO mogobiz;

--
-- Name: google_category; Type: TABLE; Schema: public; Owner: mogobiz; Tablespace: 
--

CREATE TABLE google_category (
    id bigint NOT NULL,
    date_created timestamp without time zone NOT NULL,
    lang character varying(255) NOT NULL,
    last_updated timestamp without time zone NOT NULL,
    name character varying(255) NOT NULL,
    parent_path character varying(512),
    path character varying(512) NOT NULL,
    uuid character varying(255) NOT NULL
);


ALTER TABLE public.google_category OWNER TO mogobiz;

--
-- Name: google_content; Type: TABLE; Schema: public; Owner: mogobiz; Tablespace: 
--

CREATE TABLE google_content (
    id bigint NOT NULL,
    account_id character varying(255) NOT NULL,
    account_login character varying(255) NOT NULL,
    account_password character varying(255) NOT NULL,
    account_type character varying(255) NOT NULL,
    date_created timestamp without time zone NOT NULL,
    google_search boolean NOT NULL,
    last_updated timestamp without time zone NOT NULL,
    uuid character varying(255) NOT NULL
);


ALTER TABLE public.google_content OWNER TO mogobiz;

--
-- Name: google_env; Type: TABLE; Schema: public; Owner: mogobiz; Tablespace: 
--

CREATE TABLE google_env (
    id bigint NOT NULL,
    active boolean NOT NULL,
    client_id character varying(255),
    client_secret character varying(255),
    client_token character varying(255),
    cron_expr character varying(255) NOT NULL,
    date_created timestamp without time zone NOT NULL,
    dry_run boolean NOT NULL,
    extra text,
    last_updated timestamp without time zone NOT NULL,
    merchant_id character varying(255) NOT NULL,
    merchant_url character varying(255),
    running boolean NOT NULL,
    uuid character varying(255) NOT NULL
);


ALTER TABLE public.google_env OWNER TO mogobiz;

--
-- Name: google_variation_mapping; Type: TABLE; Schema: public; Owner: mogobiz; Tablespace: 
--

CREATE TABLE google_variation_mapping (
    id bigint NOT NULL,
    company_fk bigint NOT NULL,
    date_created timestamp without time zone NOT NULL,
    last_updated timestamp without time zone NOT NULL,
    mappings character varying(255) NOT NULL,
    type_fk bigint,
    uuid character varying(255) NOT NULL,
    value_fk bigint
);


ALTER TABLE public.google_variation_mapping OWNER TO mogobiz;

--
-- Name: google_variation_type; Type: TABLE; Schema: public; Owner: mogobiz; Tablespace: 
--

CREATE TABLE google_variation_type (
    id bigint NOT NULL,
    date_created timestamp without time zone NOT NULL,
    last_updated timestamp without time zone NOT NULL,
    uuid character varying(255) NOT NULL,
    xtype character varying(255) NOT NULL
);


ALTER TABLE public.google_variation_type OWNER TO mogobiz;

--
-- Name: google_variation_value; Type: TABLE; Schema: public; Owner: mogobiz; Tablespace: 
--

CREATE TABLE google_variation_value (
    id bigint NOT NULL,
    date_created timestamp without time zone NOT NULL,
    last_updated timestamp without time zone NOT NULL,
    type_fk bigint NOT NULL,
    uuid character varying(255) NOT NULL,
    value character varying(255) NOT NULL
);


ALTER TABLE public.google_variation_value OWNER TO mogobiz;

--
-- Name: hibernate_sequence; Type: SEQUENCE; Schema: public; Owner: mogobiz
--

CREATE SEQUENCE hibernate_sequence
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.hibernate_sequence OWNER TO mogobiz;

--
-- Name: ibeacon; Type: TABLE; Schema: public; Owner: mogobiz; Tablespace: 
--

CREATE TABLE ibeacon (
    id bigint NOT NULL,
    active boolean NOT NULL,
    company_fk bigint NOT NULL,
    date_created timestamp without time zone NOT NULL,
    end_date timestamp without time zone NOT NULL,
    last_updated timestamp without time zone NOT NULL,
    major character varying(255) NOT NULL,
    minor character varying(255) NOT NULL,
    name character varying(255) NOT NULL,
    start_date timestamp without time zone NOT NULL,
    uuid character varying(255) NOT NULL
);


ALTER TABLE public.ibeacon OWNER TO mogobiz;

--
-- Name: intra_day_period; Type: TABLE; Schema: public; Owner: mogobiz; Tablespace: 
--

CREATE TABLE intra_day_period (
    id bigint NOT NULL,
    date_created timestamp without time zone NOT NULL,
    end_date timestamp without time zone,
    last_updated timestamp without time zone NOT NULL,
    product_fk bigint,
    start_date timestamp without time zone,
    uuid character varying(255) NOT NULL,
    weekday1 boolean NOT NULL,
    weekday2 boolean NOT NULL,
    weekday3 boolean NOT NULL,
    weekday4 boolean NOT NULL,
    weekday5 boolean NOT NULL,
    weekday6 boolean NOT NULL,
    weekday7 boolean NOT NULL
);


ALTER TABLE public.intra_day_period OWNER TO mogobiz;

--
-- Name: local_tax_rate; Type: TABLE; Schema: public; Owner: mogobiz; Tablespace: 
--

CREATE TABLE local_tax_rate (
    id bigint NOT NULL,
    active boolean NOT NULL,
    country_code character varying(255),
    date_created timestamp without time zone NOT NULL,
    last_updated timestamp without time zone NOT NULL,
    rate real NOT NULL,
    state_code character varying(255),
    uuid character varying(255) NOT NULL
);


ALTER TABLE public.local_tax_rate OWNER TO mogobiz;

--
-- Name: location; Type: TABLE; Schema: public; Owner: mogobiz; Tablespace: 
--

CREATE TABLE location (
    id bigint NOT NULL,
    city character varying(255),
    country_code character varying(255) NOT NULL,
    date_created timestamp without time zone NOT NULL,
    last_updated timestamp without time zone NOT NULL,
    latitude double precision,
    longitude double precision,
    postal_code character varying(255),
    road1 character varying(255),
    road2 character varying(255),
    road3 character varying(255),
    road_num character varying(255),
    state character varying(255),
    uuid character varying(255) NOT NULL,
    class character varying(255) NOT NULL,
    description character varying(255),
    detail text,
    external_id character varying(255),
    is_main boolean,
    max_price real,
    min_price real,
    name character varying(255),
    picture character varying(255),
    picture_type character varying(255),
    poi_type_fk bigint,
    source character varying(255),
    video character varying(255),
    visibility character varying(255)
);


ALTER TABLE public.location OWNER TO mogobiz;

--
-- Name: payment_data; Type: TABLE; Schema: public; Owner: mogobiz; Tablespace: 
--

CREATE TABLE payment_data (
    id bigint NOT NULL,
    date_created timestamp without time zone NOT NULL,
    last_updated timestamp without time zone NOT NULL,
    payload text NOT NULL,
    uuid character varying(255) NOT NULL,
    xtype character varying(255) NOT NULL
);


ALTER TABLE public.payment_data OWNER TO mogobiz;

--
-- Name: pending_email_confirmation; Type: TABLE; Schema: public; Owner: mogobiz; Tablespace: 
--

CREATE TABLE pending_email_confirmation (
    id bigint NOT NULL,
    version bigint NOT NULL,
    confirmation_event character varying(80),
    confirmation_token character varying(80) NOT NULL,
    email_address character varying(80) NOT NULL,
    "timestamp" timestamp without time zone NOT NULL,
    user_token character varying(500)
);


ALTER TABLE public.pending_email_confirmation OWNER TO mogobiz;

--
-- Name: permission; Type: TABLE; Schema: public; Owner: mogobiz; Tablespace: 
--

CREATE TABLE permission (
    id bigint NOT NULL,
    date_created timestamp without time zone NOT NULL,
    last_updated timestamp without time zone NOT NULL,
    possible_actions character varying(255) NOT NULL,
    type character varying(255) NOT NULL,
    uuid character varying(255) NOT NULL
);


ALTER TABLE public.permission OWNER TO mogobiz;

--
-- Name: pg_sequence; Type: SEQUENCE; Schema: public; Owner: mogobiz
--

CREATE SEQUENCE pg_sequence
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.pg_sequence OWNER TO mogobiz;

--
-- Name: poi_type; Type: TABLE; Schema: public; Owner: mogobiz; Tablespace: 
--

CREATE TABLE poi_type (
    id bigint NOT NULL,
    code character varying(255) NOT NULL,
    date_created timestamp without time zone NOT NULL,
    icon_fk bigint,
    last_updated timestamp without time zone NOT NULL,
    uuid character varying(255) NOT NULL,
    xtype character varying(255) NOT NULL
);


ALTER TABLE public.poi_type OWNER TO mogobiz;

--
-- Name: price; Type: TABLE; Schema: public; Owner: mogobiz; Tablespace: 
--

CREATE TABLE price (
    id bigint NOT NULL,
    customer_profile_fk bigint NOT NULL,
    date_created timestamp without time zone NOT NULL,
    last_updated timestamp without time zone NOT NULL,
    price bigint NOT NULL,
    ticket_type_fk bigint NOT NULL,
    uuid character varying(255) NOT NULL
);


ALTER TABLE public.price OWNER TO mogobiz;

--
-- Name: product; Type: TABLE; Schema: public; Owner: mogobiz; Tablespace: 
--

CREATE TABLE product (
    id bigint NOT NULL,
    availability_date timestamp without time zone,
    brand_fk bigint,
    calendar_type character varying(255),
    category_fk bigint NOT NULL,
    code character varying(255) NOT NULL,
    company_fk bigint NOT NULL,
    date_created timestamp without time zone NOT NULL,
    deleted boolean NOT NULL,
    description text,
    description_as_text text,
    download_max_delay bigint NOT NULL,
    download_max_times bigint NOT NULL,
    external_code character varying(255),
    hide boolean NOT NULL,
    ibeacon_fk bigint,
    keywords character varying(255),
    last_updated timestamp without time zone NOT NULL,
    modification_date timestamp without time zone,
    name character varying(255) NOT NULL,
    nb_sales bigint NOT NULL,
    picture character varying(255),
    poi_fk bigint,
    price bigint NOT NULL,
    return_max_delay bigint NOT NULL,
    sanitized_name character varying(255) NOT NULL,
    seller_fk bigint,
    shipping_fk bigint,
    start_date timestamp without time zone,
    start_feature_date timestamp without time zone,
    state character varying(255),
    stock_display boolean,
    stop_date timestamp without time zone,
    stop_feature_date timestamp without time zone,
    tax_rate_fk bigint,
    uuid character varying(255) NOT NULL,
    xtype character varying(255) NOT NULL
);


ALTER TABLE public.product OWNER TO mogobiz;

--
-- Name: product2_resource; Type: TABLE; Schema: public; Owner: mogobiz; Tablespace: 
--

CREATE TABLE product2_resource (
    id bigint NOT NULL,
    date_created timestamp without time zone NOT NULL,
    last_updated timestamp without time zone NOT NULL,
    "position" integer NOT NULL,
    product_fk bigint NOT NULL,
    resource_fk bigint NOT NULL,
    uuid character varying(255) NOT NULL
);


ALTER TABLE public.product2_resource OWNER TO mogobiz;

--
-- Name: product_property; Type: TABLE; Schema: public; Owner: mogobiz; Tablespace: 
--

CREATE TABLE product_property (
    id bigint NOT NULL,
    date_created timestamp without time zone NOT NULL,
    last_updated timestamp without time zone NOT NULL,
    name character varying(255) NOT NULL,
    product_fk bigint NOT NULL,
    uuid character varying(255) NOT NULL,
    value character varying(255)
);


ALTER TABLE public.product_property OWNER TO mogobiz;

--
-- Name: product_tag; Type: TABLE; Schema: public; Owner: mogobiz; Tablespace: 
--

CREATE TABLE product_tag (
    tags_fk bigint,
    tag_id bigint
);


ALTER TABLE public.product_tag OWNER TO mogobiz;

--
-- Name: reduction_rule; Type: TABLE; Schema: public; Owner: mogobiz; Tablespace: 
--

CREATE TABLE reduction_rule (
    id bigint NOT NULL,
    date_created timestamp without time zone NOT NULL,
    discount character varying(255),
    last_updated timestamp without time zone NOT NULL,
    quantity_max bigint,
    quantity_min bigint,
    uuid character varying(255) NOT NULL,
    x_purchased bigint,
    xtype character varying(255) NOT NULL,
    y_offered bigint
);


ALTER TABLE public.reduction_rule OWNER TO mogobiz;

--
-- Name: role_permission; Type: TABLE; Schema: public; Owner: mogobiz; Tablespace: 
--

CREATE TABLE role_permission (
    id bigint NOT NULL,
    actions character varying(255) NOT NULL,
    date_created timestamp without time zone NOT NULL,
    last_updated timestamp without time zone NOT NULL,
    permission_fk bigint NOT NULL,
    role_fk bigint NOT NULL,
    target character varying(255) NOT NULL,
    uuid character varying(255) NOT NULL
);


ALTER TABLE public.role_permission OWNER TO mogobiz;

--
-- Name: seller; Type: TABLE; Schema: public; Owner: mogobiz; Tablespace: 
--

CREATE TABLE seller (
    id bigint NOT NULL,
    admin boolean NOT NULL,
    agent boolean NOT NULL,
    sell boolean NOT NULL,
    validator boolean NOT NULL
);


ALTER TABLE public.seller OWNER TO mogobiz;

--
-- Name: seller_company; Type: TABLE; Schema: public; Owner: mogobiz; Tablespace: 
--

CREATE TABLE seller_company (
    companies_fk bigint,
    company_id bigint
);


ALTER TABLE public.seller_company OWNER TO mogobiz;

--
-- Name: shipping; Type: TABLE; Schema: public; Owner: mogobiz; Tablespace: 
--

CREATE TABLE shipping (
    id bigint NOT NULL,
    amount bigint NOT NULL,
    date_created timestamp without time zone NOT NULL,
    depth bigint NOT NULL,
    free boolean NOT NULL,
    height bigint NOT NULL,
    last_updated timestamp without time zone NOT NULL,
    linear_unit character varying(255) NOT NULL,
    uuid character varying(255) NOT NULL,
    weight bigint NOT NULL,
    weight_unit character varying(255) NOT NULL,
    width bigint NOT NULL
);


ALTER TABLE public.shipping OWNER TO mogobiz;

--
-- Name: shipping_carriers; Type: TABLE; Schema: public; Owner: mogobiz; Tablespace: 
--

CREATE TABLE shipping_carriers (
    id bigint NOT NULL,
    fedex boolean NOT NULL,
    ups boolean NOT NULL
);


ALTER TABLE public.shipping_carriers OWNER TO mogobiz;

--
-- Name: shipping_rule; Type: TABLE; Schema: public; Owner: mogobiz; Tablespace: 
--

CREATE TABLE shipping_rule (
    id bigint NOT NULL,
    company_fk bigint,
    country_code character varying(255) NOT NULL,
    date_created timestamp without time zone NOT NULL,
    last_updated timestamp without time zone NOT NULL,
    max_amount bigint NOT NULL,
    min_amount bigint NOT NULL,
    price character varying(255) NOT NULL,
    uuid character varying(255) NOT NULL
);


ALTER TABLE public.shipping_rule OWNER TO mogobiz;

--
-- Name: stock; Type: TABLE; Schema: public; Owner: mogobiz; Tablespace: 
--

CREATE TABLE stock (
    id bigint NOT NULL,
    stock bigint,
    stock_out_selling boolean NOT NULL,
    stock_unlimited boolean NOT NULL
);


ALTER TABLE public.stock OWNER TO mogobiz;

--
-- Name: stock_calendar; Type: TABLE; Schema: public; Owner: mogobiz; Tablespace: 
--

CREATE TABLE stock_calendar (
    id bigint NOT NULL,
    date_created timestamp without time zone NOT NULL,
    last_updated timestamp without time zone NOT NULL,
    product_fk bigint NOT NULL,
    sold bigint NOT NULL,
    start_date timestamp without time zone,
    stock bigint NOT NULL,
    ticket_type_fk bigint NOT NULL,
    uuid character varying(255) NOT NULL
);


ALTER TABLE public.stock_calendar OWNER TO mogobiz;

--
-- Name: suggestion; Type: TABLE; Schema: public; Owner: mogobiz; Tablespace: 
--

CREATE TABLE suggestion (
    id bigint NOT NULL,
    date_created timestamp without time zone NOT NULL,
    discount character varying(255),
    last_updated timestamp without time zone NOT NULL,
    pack_fk bigint NOT NULL,
    "position" integer NOT NULL,
    product_fk bigint NOT NULL,
    required boolean NOT NULL,
    uuid character varying(255) NOT NULL
);


ALTER TABLE public.suggestion OWNER TO mogobiz;

--
-- Name: tag; Type: TABLE; Schema: public; Owner: mogobiz; Tablespace: 
--

CREATE TABLE tag (
    id bigint NOT NULL,
    company_fk bigint NOT NULL,
    date_created timestamp without time zone NOT NULL,
    ibeacon_fk bigint,
    last_updated timestamp without time zone NOT NULL,
    name character varying(255) NOT NULL,
    uuid character varying(255) NOT NULL
);


ALTER TABLE public.tag OWNER TO mogobiz;

--
-- Name: tax_rate; Type: TABLE; Schema: public; Owner: mogobiz; Tablespace: 
--

CREATE TABLE tax_rate (
    id bigint NOT NULL,
    company_fk bigint,
    date_created timestamp without time zone NOT NULL,
    last_updated timestamp without time zone NOT NULL,
    name character varying(255) NOT NULL,
    uuid character varying(255) NOT NULL
);


ALTER TABLE public.tax_rate OWNER TO mogobiz;

--
-- Name: tax_rate_local_tax_rate; Type: TABLE; Schema: public; Owner: mogobiz; Tablespace: 
--

CREATE TABLE tax_rate_local_tax_rate (
    local_tax_rates_fk bigint,
    local_tax_rate_id bigint
);


ALTER TABLE public.tax_rate_local_tax_rate OWNER TO mogobiz;

--
-- Name: ticket_type; Type: TABLE; Schema: public; Owner: mogobiz; Tablespace: 
--

CREATE TABLE ticket_type (
    id bigint NOT NULL,
    availability_date timestamp without time zone,
    date_created timestamp without time zone NOT NULL,
    description text,
    external_code character varying(255),
    gtin character varying(255),
    last_updated timestamp without time zone NOT NULL,
    max_order integer NOT NULL,
    min_order integer NOT NULL,
    mpn character varying(255),
    name character varying(255) NOT NULL,
    nb_sales bigint NOT NULL,
    picture_fk bigint,
    "position" integer,
    price bigint NOT NULL,
    product_fk bigint NOT NULL,
    sku character varying(255) NOT NULL,
    start_date timestamp without time zone,
    stock_stock bigint,
    stock_stock_out_selling boolean,
    stock_stock_unlimited boolean,
    stop_date timestamp without time zone,
    uuid character varying(255) NOT NULL,
    variation1_fk bigint,
    variation2_fk bigint,
    variation3_fk bigint,
    xprivate boolean
);


ALTER TABLE public.ticket_type OWNER TO mogobiz;

--
-- Name: token; Type: TABLE; Schema: public; Owner: mogobiz; Tablespace: 
--

CREATE TABLE token (
    id bigint NOT NULL,
    client_id character varying(255) NOT NULL,
    date_created timestamp without time zone NOT NULL,
    expires_in integer NOT NULL,
    last_updated timestamp without time zone NOT NULL,
    redirect_u_r_i character varying(255) NOT NULL,
    scope character varying(255),
    state character varying(255),
    user_fk bigint NOT NULL,
    uuid character varying(255) NOT NULL,
    value character varying(255) NOT NULL
);


ALTER TABLE public.token OWNER TO mogobiz;

--
-- Name: user_permission; Type: TABLE; Schema: public; Owner: mogobiz; Tablespace: 
--

CREATE TABLE user_permission (
    id bigint NOT NULL,
    actions character varying(255) NOT NULL,
    date_created timestamp without time zone NOT NULL,
    last_updated timestamp without time zone NOT NULL,
    permission_fk bigint NOT NULL,
    target character varying(255) NOT NULL,
    user_fk bigint NOT NULL,
    uuid character varying(255) NOT NULL
);


ALTER TABLE public.user_permission OWNER TO mogobiz;

--
-- Name: user_property; Type: TABLE; Schema: public; Owner: mogobiz; Tablespace: 
--

CREATE TABLE user_property (
    id bigint NOT NULL,
    date_created timestamp without time zone NOT NULL,
    last_updated timestamp without time zone NOT NULL,
    name character varying(255) NOT NULL,
    user_fk bigint NOT NULL,
    uuid character varying(255) NOT NULL,
    value character varying(255)
);


ALTER TABLE public.user_property OWNER TO mogobiz;

--
-- Name: variation; Type: TABLE; Schema: public; Owner: mogobiz; Tablespace: 
--

CREATE TABLE variation (
    id bigint NOT NULL,
    category_fk bigint,
    date_created timestamp without time zone NOT NULL,
    external_code character varying(255),
    google_variation_type character varying(255),
    hide boolean NOT NULL,
    last_updated timestamp without time zone NOT NULL,
    name character varying(255) NOT NULL,
    "position" integer NOT NULL,
    uuid character varying(255) NOT NULL
);


ALTER TABLE public.variation OWNER TO mogobiz;

--
-- Name: variation_value; Type: TABLE; Schema: public; Owner: mogobiz; Tablespace: 
--

CREATE TABLE variation_value (
    id bigint NOT NULL,
    date_created timestamp without time zone NOT NULL,
    external_code character varying(255),
    google_variation_value character varying(255),
    last_updated timestamp without time zone NOT NULL,
    "position" integer NOT NULL,
    uuid character varying(255) NOT NULL,
    value character varying(255) NOT NULL,
    variation_fk bigint NOT NULL
);


ALTER TABLE public.variation_value OWNER TO mogobiz;

--
-- Name: warehouse; Type: TABLE; Schema: public; Owner: mogobiz; Tablespace: 
--

CREATE TABLE warehouse (
    id bigint NOT NULL,
    code character varying(255) NOT NULL,
    date_created timestamp without time zone NOT NULL,
    external_code character varying(255),
    last_updated timestamp without time zone NOT NULL,
    name character varying(255) NOT NULL,
    pick_delay_in_minutes bigint NOT NULL,
    ship_from_fk bigint,
    uuid character varying(255) NOT NULL
);


ALTER TABLE public.warehouse OWNER TO mogobiz;

--
-- Name: xcatalog; Type: TABLE; Schema: public; Owner: mogobiz; Tablespace: 
--

CREATE TABLE xcatalog (
    id bigint NOT NULL,
    activation_date timestamp without time zone NOT NULL,
    channels character varying(255),
    company_fk bigint NOT NULL,
    date_created timestamp without time zone NOT NULL,
    deleted boolean NOT NULL,
    description text,
    external_code character varying(255),
    last_updated timestamp without time zone NOT NULL,
    name character varying(255) NOT NULL,
    return_max_delay bigint NOT NULL,
    social boolean NOT NULL,
    uuid character varying(255) NOT NULL,
    xcatalog character varying(255)
);


ALTER TABLE public.xcatalog OWNER TO mogobiz;

--
-- Name: xresource; Type: TABLE; Schema: public; Owner: mogobiz; Tablespace: 
--

CREATE TABLE xresource (
    id bigint NOT NULL,
    account_type character varying(255),
    active boolean NOT NULL,
    album_fk bigint,
    code character varying(255),
    company_fk bigint,
    content text,
    content_type character varying(255),
    date_created timestamp without time zone NOT NULL,
    deleted boolean NOT NULL,
    description text,
    external_code character varying(255),
    last_updated timestamp without time zone NOT NULL,
    name character varying(255),
    poi_fk bigint,
    sanitized_name character varying(255) NOT NULL,
    small_picture character varying(255),
    uploaded boolean NOT NULL,
    url character varying(255),
    uuid character varying(255) NOT NULL,
    xtype character varying(255) NOT NULL
);


ALTER TABLE public.xresource OWNER TO mogobiz;

--
-- Name: xrole; Type: TABLE; Schema: public; Owner: mogobiz; Tablespace: 
--

CREATE TABLE xrole (
    id bigint NOT NULL,
    date_created timestamp without time zone NOT NULL,
    last_updated timestamp without time zone NOT NULL,
    name character varying(255) NOT NULL,
    uuid character varying(255) NOT NULL
);


ALTER TABLE public.xrole OWNER TO mogobiz;

--
-- Name: xtranslation; Type: TABLE; Schema: public; Owner: mogobiz; Tablespace: 
--

CREATE TABLE xtranslation (
    id bigint NOT NULL,
    company_id bigint NOT NULL,
    date_created timestamp without time zone NOT NULL,
    lang character varying(255) NOT NULL,
    last_updated timestamp without time zone NOT NULL,
    target bigint NOT NULL,
    type character varying(255),
    uuid character varying(255) NOT NULL,
    value text NOT NULL
);


ALTER TABLE public.xtranslation OWNER TO mogobiz;

--
-- Name: account_email_key; Type: CONSTRAINT; Schema: public; Owner: mogobiz; Tablespace: 
--

ALTER TABLE ONLY account
    ADD CONSTRAINT account_email_key UNIQUE (email);


--
-- Name: account_login_key; Type: CONSTRAINT; Schema: public; Owner: mogobiz; Tablespace: 
--

ALTER TABLE ONLY account
    ADD CONSTRAINT account_login_key UNIQUE (login);


--
-- Name: account_pkey; Type: CONSTRAINT; Schema: public; Owner: mogobiz; Tablespace: 
--

ALTER TABLE ONLY account
    ADD CONSTRAINT account_pkey PRIMARY KEY (id);


--
-- Name: album_pkey; Type: CONSTRAINT; Schema: public; Owner: mogobiz; Tablespace: 
--

ALTER TABLE ONLY album
    ADD CONSTRAINT album_pkey PRIMARY KEY (id);


--
-- Name: b_o_account_pkey; Type: CONSTRAINT; Schema: public; Owner: mogobiz; Tablespace: 
--

ALTER TABLE ONLY b_o_account
    ADD CONSTRAINT b_o_account_pkey PRIMARY KEY (id);


--
-- Name: b_o_cart_item_code_key; Type: CONSTRAINT; Schema: public; Owner: mogobiz; Tablespace: 
--

ALTER TABLE ONLY b_o_cart_item
    ADD CONSTRAINT b_o_cart_item_code_key UNIQUE (code);


--
-- Name: b_o_cart_item_pkey; Type: CONSTRAINT; Schema: public; Owner: mogobiz; Tablespace: 
--

ALTER TABLE ONLY b_o_cart_item
    ADD CONSTRAINT b_o_cart_item_pkey PRIMARY KEY (id);


--
-- Name: b_o_cart_pkey; Type: CONSTRAINT; Schema: public; Owner: mogobiz; Tablespace: 
--

ALTER TABLE ONLY b_o_cart
    ADD CONSTRAINT b_o_cart_pkey PRIMARY KEY (id);


--
-- Name: b_o_cart_transaction_uuid_key; Type: CONSTRAINT; Schema: public; Owner: mogobiz; Tablespace: 
--

ALTER TABLE ONLY b_o_cart
    ADD CONSTRAINT b_o_cart_transaction_uuid_key UNIQUE (transaction_uuid);


--
-- Name: b_o_delivery_pkey; Type: CONSTRAINT; Schema: public; Owner: mogobiz; Tablespace: 
--

ALTER TABLE ONLY b_o_delivery
    ADD CONSTRAINT b_o_delivery_pkey PRIMARY KEY (id);


--
-- Name: b_o_product_pkey; Type: CONSTRAINT; Schema: public; Owner: mogobiz; Tablespace: 
--

ALTER TABLE ONLY b_o_product
    ADD CONSTRAINT b_o_product_pkey PRIMARY KEY (id);


--
-- Name: b_o_return_pkey; Type: CONSTRAINT; Schema: public; Owner: mogobiz; Tablespace: 
--

ALTER TABLE ONLY b_o_return
    ADD CONSTRAINT b_o_return_pkey PRIMARY KEY (id);


--
-- Name: b_o_returned_item_pkey; Type: CONSTRAINT; Schema: public; Owner: mogobiz; Tablespace: 
--

ALTER TABLE ONLY b_o_returned_item
    ADD CONSTRAINT b_o_returned_item_pkey PRIMARY KEY (id);


--
-- Name: b_o_ticket_type_pkey; Type: CONSTRAINT; Schema: public; Owner: mogobiz; Tablespace: 
--

ALTER TABLE ONLY b_o_ticket_type
    ADD CONSTRAINT b_o_ticket_type_pkey PRIMARY KEY (id);


--
-- Name: b_o_transaction_pkey; Type: CONSTRAINT; Schema: public; Owner: mogobiz; Tablespace: 
--

ALTER TABLE ONLY b_o_transaction
    ADD CONSTRAINT b_o_transaction_pkey PRIMARY KEY (id);


--
-- Name: brand_pkey; Type: CONSTRAINT; Schema: public; Owner: mogobiz; Tablespace: 
--

ALTER TABLE ONLY brand
    ADD CONSTRAINT brand_pkey PRIMARY KEY (id);


--
-- Name: brand_property_pkey; Type: CONSTRAINT; Schema: public; Owner: mogobiz; Tablespace: 
--

ALTER TABLE ONLY brand_property
    ADD CONSTRAINT brand_property_pkey PRIMARY KEY (id);


--
-- Name: category_pkey; Type: CONSTRAINT; Schema: public; Owner: mogobiz; Tablespace: 
--

ALTER TABLE ONLY category
    ADD CONSTRAINT category_pkey PRIMARY KEY (id);


--
-- Name: company_pkey; Type: CONSTRAINT; Schema: public; Owner: mogobiz; Tablespace: 
--

ALTER TABLE ONLY company
    ADD CONSTRAINT company_pkey PRIMARY KEY (id);


--
-- Name: company_property_pkey; Type: CONSTRAINT; Schema: public; Owner: mogobiz; Tablespace: 
--

ALTER TABLE ONLY company_property
    ADD CONSTRAINT company_property_pkey PRIMARY KEY (id);


--
-- Name: consumption_pkey; Type: CONSTRAINT; Schema: public; Owner: mogobiz; Tablespace: 
--

ALTER TABLE ONLY consumption
    ADD CONSTRAINT consumption_pkey PRIMARY KEY (id);


--
-- Name: country_admin_pkey; Type: CONSTRAINT; Schema: public; Owner: mogobiz; Tablespace: 
--

ALTER TABLE ONLY country_admin
    ADD CONSTRAINT country_admin_pkey PRIMARY KEY (id);


--
-- Name: country_pkey; Type: CONSTRAINT; Schema: public; Owner: mogobiz; Tablespace: 
--

ALTER TABLE ONLY country
    ADD CONSTRAINT country_pkey PRIMARY KEY (code);


--
-- Name: coupon_pkey; Type: CONSTRAINT; Schema: public; Owner: mogobiz; Tablespace: 
--

ALTER TABLE ONLY coupon
    ADD CONSTRAINT coupon_pkey PRIMARY KEY (id);


--
-- Name: customer_profile_pkey; Type: CONSTRAINT; Schema: public; Owner: mogobiz; Tablespace: 
--

ALTER TABLE ONLY customer_profile
    ADD CONSTRAINT customer_profile_pkey PRIMARY KEY (id);


--
-- Name: date_period_pkey; Type: CONSTRAINT; Schema: public; Owner: mogobiz; Tablespace: 
--

ALTER TABLE ONLY date_period
    ADD CONSTRAINT date_period_pkey PRIMARY KEY (id);


--
-- Name: es_env_pkey; Type: CONSTRAINT; Schema: public; Owner: mogobiz; Tablespace: 
--

ALTER TABLE ONLY es_env
    ADD CONSTRAINT es_env_pkey PRIMARY KEY (id);


--
-- Name: event_period_sale_pkey; Type: CONSTRAINT; Schema: public; Owner: mogobiz; Tablespace: 
--

ALTER TABLE ONLY event_period_sale
    ADD CONSTRAINT event_period_sale_pkey PRIMARY KEY (id);


--
-- Name: external_account_pkey; Type: CONSTRAINT; Schema: public; Owner: mogobiz; Tablespace: 
--

ALTER TABLE ONLY external_account
    ADD CONSTRAINT external_account_pkey PRIMARY KEY (id);


--
-- Name: external_auth_login_pkey; Type: CONSTRAINT; Schema: public; Owner: mogobiz; Tablespace: 
--

ALTER TABLE ONLY external_auth_login
    ADD CONSTRAINT external_auth_login_pkey PRIMARY KEY (id);


--
-- Name: feature_pkey; Type: CONSTRAINT; Schema: public; Owner: mogobiz; Tablespace: 
--

ALTER TABLE ONLY feature
    ADD CONSTRAINT feature_pkey PRIMARY KEY (id);


--
-- Name: feature_value_pkey; Type: CONSTRAINT; Schema: public; Owner: mogobiz; Tablespace: 
--

ALTER TABLE ONLY feature_value
    ADD CONSTRAINT feature_value_pkey PRIMARY KEY (id);


--
-- Name: google_category_pkey; Type: CONSTRAINT; Schema: public; Owner: mogobiz; Tablespace: 
--

ALTER TABLE ONLY google_category
    ADD CONSTRAINT google_category_pkey PRIMARY KEY (id);


--
-- Name: google_content_pkey; Type: CONSTRAINT; Schema: public; Owner: mogobiz; Tablespace: 
--

ALTER TABLE ONLY google_content
    ADD CONSTRAINT google_content_pkey PRIMARY KEY (id);


--
-- Name: google_env_pkey; Type: CONSTRAINT; Schema: public; Owner: mogobiz; Tablespace: 
--

ALTER TABLE ONLY google_env
    ADD CONSTRAINT google_env_pkey PRIMARY KEY (id);


--
-- Name: google_variation_mapping_pkey; Type: CONSTRAINT; Schema: public; Owner: mogobiz; Tablespace: 
--

ALTER TABLE ONLY google_variation_mapping
    ADD CONSTRAINT google_variation_mapping_pkey PRIMARY KEY (id);


--
-- Name: google_variation_type_pkey; Type: CONSTRAINT; Schema: public; Owner: mogobiz; Tablespace: 
--

ALTER TABLE ONLY google_variation_type
    ADD CONSTRAINT google_variation_type_pkey PRIMARY KEY (id);


--
-- Name: google_variation_type_xtype_key; Type: CONSTRAINT; Schema: public; Owner: mogobiz; Tablespace: 
--

ALTER TABLE ONLY google_variation_type
    ADD CONSTRAINT google_variation_type_xtype_key UNIQUE (xtype);


--
-- Name: google_variation_value_pkey; Type: CONSTRAINT; Schema: public; Owner: mogobiz; Tablespace: 
--

ALTER TABLE ONLY google_variation_value
    ADD CONSTRAINT google_variation_value_pkey PRIMARY KEY (id);


--
-- Name: google_variation_value_value_key; Type: CONSTRAINT; Schema: public; Owner: mogobiz; Tablespace: 
--

ALTER TABLE ONLY google_variation_value
    ADD CONSTRAINT google_variation_value_value_key UNIQUE (value);


--
-- Name: ibeacon_pkey; Type: CONSTRAINT; Schema: public; Owner: mogobiz; Tablespace: 
--

ALTER TABLE ONLY ibeacon
    ADD CONSTRAINT ibeacon_pkey PRIMARY KEY (id);


--
-- Name: intra_day_period_pkey; Type: CONSTRAINT; Schema: public; Owner: mogobiz; Tablespace: 
--

ALTER TABLE ONLY intra_day_period
    ADD CONSTRAINT intra_day_period_pkey PRIMARY KEY (id);


--
-- Name: local_tax_rate_pkey; Type: CONSTRAINT; Schema: public; Owner: mogobiz; Tablespace: 
--

ALTER TABLE ONLY local_tax_rate
    ADD CONSTRAINT local_tax_rate_pkey PRIMARY KEY (id);


--
-- Name: location_pkey; Type: CONSTRAINT; Schema: public; Owner: mogobiz; Tablespace: 
--

ALTER TABLE ONLY location
    ADD CONSTRAINT location_pkey PRIMARY KEY (id);


--
-- Name: payment_data_pkey; Type: CONSTRAINT; Schema: public; Owner: mogobiz; Tablespace: 
--

ALTER TABLE ONLY payment_data
    ADD CONSTRAINT payment_data_pkey PRIMARY KEY (id);


--
-- Name: pending_email_confirmation_confirmation_token_key; Type: CONSTRAINT; Schema: public; Owner: mogobiz; Tablespace: 
--

ALTER TABLE ONLY pending_email_confirmation
    ADD CONSTRAINT pending_email_confirmation_confirmation_token_key UNIQUE (confirmation_token);


--
-- Name: pending_email_confirmation_pkey; Type: CONSTRAINT; Schema: public; Owner: mogobiz; Tablespace: 
--

ALTER TABLE ONLY pending_email_confirmation
    ADD CONSTRAINT pending_email_confirmation_pkey PRIMARY KEY (id);


--
-- Name: permission_pkey; Type: CONSTRAINT; Schema: public; Owner: mogobiz; Tablespace: 
--

ALTER TABLE ONLY permission
    ADD CONSTRAINT permission_pkey PRIMARY KEY (id);


--
-- Name: poi_type_pkey; Type: CONSTRAINT; Schema: public; Owner: mogobiz; Tablespace: 
--

ALTER TABLE ONLY poi_type
    ADD CONSTRAINT poi_type_pkey PRIMARY KEY (id);


--
-- Name: poi_type_xtype_key; Type: CONSTRAINT; Schema: public; Owner: mogobiz; Tablespace: 
--

ALTER TABLE ONLY poi_type
    ADD CONSTRAINT poi_type_xtype_key UNIQUE (xtype);


--
-- Name: price_pkey; Type: CONSTRAINT; Schema: public; Owner: mogobiz; Tablespace: 
--

ALTER TABLE ONLY price
    ADD CONSTRAINT price_pkey PRIMARY KEY (id);


--
-- Name: product2_resource_pkey; Type: CONSTRAINT; Schema: public; Owner: mogobiz; Tablespace: 
--

ALTER TABLE ONLY product2_resource
    ADD CONSTRAINT product2_resource_pkey PRIMARY KEY (id);


--
-- Name: product_pkey; Type: CONSTRAINT; Schema: public; Owner: mogobiz; Tablespace: 
--

ALTER TABLE ONLY product
    ADD CONSTRAINT product_pkey PRIMARY KEY (id);


--
-- Name: product_property_pkey; Type: CONSTRAINT; Schema: public; Owner: mogobiz; Tablespace: 
--

ALTER TABLE ONLY product_property
    ADD CONSTRAINT product_property_pkey PRIMARY KEY (id);


--
-- Name: reduction_rule_pkey; Type: CONSTRAINT; Schema: public; Owner: mogobiz; Tablespace: 
--

ALTER TABLE ONLY reduction_rule
    ADD CONSTRAINT reduction_rule_pkey PRIMARY KEY (id);


--
-- Name: role_permission_pkey; Type: CONSTRAINT; Schema: public; Owner: mogobiz; Tablespace: 
--

ALTER TABLE ONLY role_permission
    ADD CONSTRAINT role_permission_pkey PRIMARY KEY (id);


--
-- Name: seller_pkey; Type: CONSTRAINT; Schema: public; Owner: mogobiz; Tablespace: 
--

ALTER TABLE ONLY seller
    ADD CONSTRAINT seller_pkey PRIMARY KEY (id);


--
-- Name: shipping_carriers_pkey; Type: CONSTRAINT; Schema: public; Owner: mogobiz; Tablespace: 
--

ALTER TABLE ONLY shipping_carriers
    ADD CONSTRAINT shipping_carriers_pkey PRIMARY KEY (id);


--
-- Name: shipping_pkey; Type: CONSTRAINT; Schema: public; Owner: mogobiz; Tablespace: 
--

ALTER TABLE ONLY shipping
    ADD CONSTRAINT shipping_pkey PRIMARY KEY (id);


--
-- Name: shipping_rule_pkey; Type: CONSTRAINT; Schema: public; Owner: mogobiz; Tablespace: 
--

ALTER TABLE ONLY shipping_rule
    ADD CONSTRAINT shipping_rule_pkey PRIMARY KEY (id);


--
-- Name: stock_calendar_pkey; Type: CONSTRAINT; Schema: public; Owner: mogobiz; Tablespace: 
--

ALTER TABLE ONLY stock_calendar
    ADD CONSTRAINT stock_calendar_pkey PRIMARY KEY (id);


--
-- Name: stock_pkey; Type: CONSTRAINT; Schema: public; Owner: mogobiz; Tablespace: 
--

ALTER TABLE ONLY stock
    ADD CONSTRAINT stock_pkey PRIMARY KEY (id);


--
-- Name: suggestion_pkey; Type: CONSTRAINT; Schema: public; Owner: mogobiz; Tablespace: 
--

ALTER TABLE ONLY suggestion
    ADD CONSTRAINT suggestion_pkey PRIMARY KEY (id);


--
-- Name: tag_pkey; Type: CONSTRAINT; Schema: public; Owner: mogobiz; Tablespace: 
--

ALTER TABLE ONLY tag
    ADD CONSTRAINT tag_pkey PRIMARY KEY (id);


--
-- Name: tax_rate_pkey; Type: CONSTRAINT; Schema: public; Owner: mogobiz; Tablespace: 
--

ALTER TABLE ONLY tax_rate
    ADD CONSTRAINT tax_rate_pkey PRIMARY KEY (id);


--
-- Name: ticket_type_pkey; Type: CONSTRAINT; Schema: public; Owner: mogobiz; Tablespace: 
--

ALTER TABLE ONLY ticket_type
    ADD CONSTRAINT ticket_type_pkey PRIMARY KEY (id);


--
-- Name: token_pkey; Type: CONSTRAINT; Schema: public; Owner: mogobiz; Tablespace: 
--

ALTER TABLE ONLY token
    ADD CONSTRAINT token_pkey PRIMARY KEY (id);


--
-- Name: user_permission_pkey; Type: CONSTRAINT; Schema: public; Owner: mogobiz; Tablespace: 
--

ALTER TABLE ONLY user_permission
    ADD CONSTRAINT user_permission_pkey PRIMARY KEY (id);


--
-- Name: user_property_pkey; Type: CONSTRAINT; Schema: public; Owner: mogobiz; Tablespace: 
--

ALTER TABLE ONLY user_property
    ADD CONSTRAINT user_property_pkey PRIMARY KEY (id);


--
-- Name: variation_pkey; Type: CONSTRAINT; Schema: public; Owner: mogobiz; Tablespace: 
--

ALTER TABLE ONLY variation
    ADD CONSTRAINT variation_pkey PRIMARY KEY (id);


--
-- Name: variation_value_pkey; Type: CONSTRAINT; Schema: public; Owner: mogobiz; Tablespace: 
--

ALTER TABLE ONLY variation_value
    ADD CONSTRAINT variation_value_pkey PRIMARY KEY (id);


--
-- Name: warehouse_pkey; Type: CONSTRAINT; Schema: public; Owner: mogobiz; Tablespace: 
--

ALTER TABLE ONLY warehouse
    ADD CONSTRAINT warehouse_pkey PRIMARY KEY (id);


--
-- Name: xcatalog_pkey; Type: CONSTRAINT; Schema: public; Owner: mogobiz; Tablespace: 
--

ALTER TABLE ONLY xcatalog
    ADD CONSTRAINT xcatalog_pkey PRIMARY KEY (id);


--
-- Name: xresource_pkey; Type: CONSTRAINT; Schema: public; Owner: mogobiz; Tablespace: 
--

ALTER TABLE ONLY xresource
    ADD CONSTRAINT xresource_pkey PRIMARY KEY (id);


--
-- Name: xrole_pkey; Type: CONSTRAINT; Schema: public; Owner: mogobiz; Tablespace: 
--

ALTER TABLE ONLY xrole
    ADD CONSTRAINT xrole_pkey PRIMARY KEY (id);


--
-- Name: xtranslation_pkey; Type: CONSTRAINT; Schema: public; Owner: mogobiz; Tablespace: 
--

ALTER TABLE ONLY xtranslation
    ADD CONSTRAINT xtranslation_pkey PRIMARY KEY (id);


--
-- Name: emailconf_timestamp_idx; Type: INDEX; Schema: public; Owner: mogobiz; Tablespace: 
--

CREATE INDEX emailconf_timestamp_idx ON pending_email_confirmation USING btree ("timestamp");


--
-- Name: emailconf_token_idx; Type: INDEX; Schema: public; Owner: mogobiz; Tablespace: 
--

CREATE INDEX emailconf_token_idx ON pending_email_confirmation USING btree (confirmation_token);


--
-- Name: fk158de48d55ecafb9; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY ticket_type
    ADD CONSTRAINT fk158de48d55ecafb9 FOREIGN KEY (product_fk) REFERENCES product(id);


--
-- Name: fk158de48d62550cef; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY ticket_type
    ADD CONSTRAINT fk158de48d62550cef FOREIGN KEY (variation1_fk) REFERENCES variation_value(id);


--
-- Name: fk158de48d6255814e; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY ticket_type
    ADD CONSTRAINT fk158de48d6255814e FOREIGN KEY (variation2_fk) REFERENCES variation_value(id);


--
-- Name: fk158de48d6255f5ad; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY ticket_type
    ADD CONSTRAINT fk158de48d6255f5ad FOREIGN KEY (variation3_fk) REFERENCES variation_value(id);


--
-- Name: fk158de48d8ac3cd9f; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY ticket_type
    ADD CONSTRAINT fk158de48d8ac3cd9f FOREIGN KEY (picture_fk) REFERENCES xresource(id);


--
-- Name: fk1af5740f49ba7844; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY poi_type
    ADD CONSTRAINT fk1af5740f49ba7844 FOREIGN KEY (icon_fk) REFERENCES xresource(id);


--
-- Name: fk1bf9a602dcf9; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY tag
    ADD CONSTRAINT fk1bf9a602dcf9 FOREIGN KEY (company_fk) REFERENCES company(id);


--
-- Name: fk1bf9a9eca6f9; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY tag
    ADD CONSTRAINT fk1bf9a9eca6f9 FOREIGN KEY (ibeacon_fk) REFERENCES ibeacon(id);


--
-- Name: fk2062ceed602dcf9; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY shipping_rule
    ADD CONSTRAINT fk2062ceed602dcf9 FOREIGN KEY (company_fk) REFERENCES company(id);


--
-- Name: fk22f2de055ecafb9; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY event_period_sale
    ADD CONSTRAINT fk22f2de055ecafb9 FOREIGN KEY (product_fk) REFERENCES product(id);


--
-- Name: fk28764b6d602dcf9; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY b_o_cart
    ADD CONSTRAINT fk28764b6d602dcf9 FOREIGN KEY (company_fk) REFERENCES company(id);


--
-- Name: fk302bcfe602dcf9; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY category
    ADD CONSTRAINT fk302bcfe602dcf9 FOREIGN KEY (company_fk) REFERENCES company(id);


--
-- Name: fk302bcfe7aca6b63; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY category
    ADD CONSTRAINT fk302bcfe7aca6b63 FOREIGN KEY (parent_fk) REFERENCES category(id);


--
-- Name: fk302bcfe9eca6f9; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY category
    ADD CONSTRAINT fk302bcfe9eca6f9 FOREIGN KEY (ibeacon_fk) REFERENCES ibeacon(id);


--
-- Name: fk302bcfea332e779; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY category
    ADD CONSTRAINT fk302bcfea332e779 FOREIGN KEY (catalog_fk) REFERENCES xcatalog(id);


--
-- Name: fk30ba72c3793c84ef; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY user_permission
    ADD CONSTRAINT fk30ba72c3793c84ef FOREIGN KEY (user_fk) REFERENCES account(id);


--
-- Name: fk30ba72c3f3e69a2f; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY user_permission
    ADD CONSTRAINT fk30ba72c3f3e69a2f FOREIGN KEY (permission_fk) REFERENCES permission(id);


--
-- Name: fk38a73c7d4be157d4; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY company
    ADD CONSTRAINT fk38a73c7d4be157d4 FOREIGN KEY (location_fk) REFERENCES location(id);


--
-- Name: fk38a73c7d501695f9; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY company
    ADD CONSTRAINT fk38a73c7d501695f9 FOREIGN KEY (warehouse_fk) REFERENCES warehouse(id);


--
-- Name: fk38a73c7d66c87106; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY company
    ADD CONSTRAINT fk38a73c7d66c87106 FOREIGN KEY (google_content_fk) REFERENCES google_content(id);


--
-- Name: fk38a73c7d95de64bc; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY company
    ADD CONSTRAINT fk38a73c7d95de64bc FOREIGN KEY (ship_from_fk) REFERENCES location(id);


--
-- Name: fk38a73c7de9da1d86; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY company
    ADD CONSTRAINT fk38a73c7de9da1d86 FOREIGN KEY (google_env_fk) REFERENCES google_env(id);


--
-- Name: fk3be2b281169fc5de; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY coupon_reduction_rule
    ADD CONSTRAINT fk3be2b281169fc5de FOREIGN KEY (rules_fk) REFERENCES coupon(id);


--
-- Name: fk3be2b2818af42876; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY coupon_reduction_rule
    ADD CONSTRAINT fk3be2b2818af42876 FOREIGN KEY (reduction_rule_id) REFERENCES reduction_rule(id);


--
-- Name: fk4763ca0455ecafb9; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY suggestion
    ADD CONSTRAINT fk4763ca0455ecafb9 FOREIGN KEY (product_fk) REFERENCES product(id);


--
-- Name: fk4763ca04bd55124f; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY suggestion
    ADD CONSTRAINT fk4763ca04bd55124f FOREIGN KEY (pack_fk) REFERENCES product(id);


--
-- Name: fk4a56237255ecafb9; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY date_period
    ADD CONSTRAINT fk4a56237255ecafb9 FOREIGN KEY (product_fk) REFERENCES product(id);


--
-- Name: fk5531fdc183966c6f; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY b_o_delivery
    ADD CONSTRAINT fk5531fdc183966c6f FOREIGN KEY (b_o_cart_fk) REFERENCES b_o_cart(id);


--
-- Name: fk5897e6f602dcf9; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY album
    ADD CONSTRAINT fk5897e6f602dcf9 FOREIGN KEY (company_fk) REFERENCES company(id);


--
-- Name: fk59a4b87388bbdf6; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY brand
    ADD CONSTRAINT fk59a4b87388bbdf6 FOREIGN KEY (parent_fk) REFERENCES brand(id);


--
-- Name: fk59a4b87602dcf9; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY brand
    ADD CONSTRAINT fk59a4b87602dcf9 FOREIGN KEY (company_fk) REFERENCES company(id);


--
-- Name: fk59a4b879eca6f9; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY brand
    ADD CONSTRAINT fk59a4b879eca6f9 FOREIGN KEY (ibeacon_fk) REFERENCES ibeacon(id);


--
-- Name: fk5a0d99b9793c84ef; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY external_account
    ADD CONSTRAINT fk5a0d99b9793c84ef FOREIGN KEY (user_fk) REFERENCES account(id);


--
-- Name: fk5f6619ed602dcf9; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY ibeacon
    ADD CONSTRAINT fk5f6619ed602dcf9 FOREIGN KEY (company_fk) REFERENCES company(id);


--
-- Name: fk60362a1c602dcf9; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY google_variation_mapping
    ADD CONSTRAINT fk60362a1c602dcf9 FOREIGN KEY (company_fk) REFERENCES company(id);


--
-- Name: fk60362a1cf0cf6b53; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY google_variation_mapping
    ADD CONSTRAINT fk60362a1cf0cf6b53 FOREIGN KEY (type_fk) REFERENCES google_variation_type(id);


--
-- Name: fk60362a1cfd6cb975; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY google_variation_mapping
    ADD CONSTRAINT fk60362a1cfd6cb975 FOREIGN KEY (value_fk) REFERENCES google_variation_value(id);


--
-- Name: fk61fb5f2555ecafb9; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY product_property
    ADD CONSTRAINT fk61fb5f2555ecafb9 FOREIGN KEY (product_fk) REFERENCES product(id);


--
-- Name: fk65fb1492fde5d48; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY price
    ADD CONSTRAINT fk65fb1492fde5d48 FOREIGN KEY (ticket_type_fk) REFERENCES ticket_type(id);


--
-- Name: fk65fb149f63639bc; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY price
    ADD CONSTRAINT fk65fb149f63639bc FOREIGN KEY (customer_profile_fk) REFERENCES customer_profile(id);


--
-- Name: fk696b9f9793c84ef; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY token
    ADD CONSTRAINT fk696b9f9793c84ef FOREIGN KEY (user_fk) REFERENCES account(id);


--
-- Name: fk6a96fd8255ecafb9; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY b_o_product
    ADD CONSTRAINT fk6a96fd8255ecafb9 FOREIGN KEY (product_fk) REFERENCES product(id);


--
-- Name: fk6bbfcc861865e8f9; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY xresource
    ADD CONSTRAINT fk6bbfcc861865e8f9 FOREIGN KEY (album_fk) REFERENCES album(id);


--
-- Name: fk6bbfcc86602dcf9; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY xresource
    ADD CONSTRAINT fk6bbfcc86602dcf9 FOREIGN KEY (company_fk) REFERENCES company(id);


--
-- Name: fk6bbfcc86bf86fc94; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY xresource
    ADD CONSTRAINT fk6bbfcc86bf86fc94 FOREIGN KEY (poi_fk) REFERENCES location(id);


--
-- Name: fk6c2d04a526ce4e6f; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY b_o_cart_item
    ADD CONSTRAINT fk6c2d04a526ce4e6f FOREIGN KEY (b_o_delivery_fk) REFERENCES b_o_delivery(id);


--
-- Name: fk6c2d04a583966c6f; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY b_o_cart_item
    ADD CONSTRAINT fk6c2d04a583966c6f FOREIGN KEY (b_o_cart_fk) REFERENCES b_o_cart(id);


--
-- Name: fk714f9fb59319bf09; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY location
    ADD CONSTRAINT fk714f9fb59319bf09 FOREIGN KEY (poi_type_fk) REFERENCES poi_type(id);


--
-- Name: fk7deb1c5e482bbe0f; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY b_o_product_consumption
    ADD CONSTRAINT fk7deb1c5e482bbe0f FOREIGN KEY (consumption_id) REFERENCES consumption(id);


--
-- Name: fk7deb1c5ec732e1a3; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY b_o_product_consumption
    ADD CONSTRAINT fk7deb1c5ec732e1a3 FOREIGN KEY (consumptions_fk) REFERENCES b_o_product(id);


--
-- Name: fk859561f3279e7339; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY tax_rate_local_tax_rate
    ADD CONSTRAINT fk859561f3279e7339 FOREIGN KEY (local_tax_rates_fk) REFERENCES tax_rate(id);


--
-- Name: fk859561f3982d71d; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY tax_rate_local_tax_rate
    ADD CONSTRAINT fk859561f3982d71d FOREIGN KEY (local_tax_rate_id) REFERENCES local_tax_rate(id);


--
-- Name: fk8730c5d655ecb00f; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY coupon_product
    ADD CONSTRAINT fk8730c5d655ecb00f FOREIGN KEY (product_id) REFERENCES product(id);


--
-- Name: fk8730c5d69035ca51; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY coupon_product
    ADD CONSTRAINT fk8730c5d69035ca51 FOREIGN KEY (products_fk) REFERENCES coupon(id);


--
-- Name: fk88ef3ac395de64bc; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY warehouse
    ADD CONSTRAINT fk88ef3ac395de64bc FOREIGN KEY (ship_from_fk) REFERENCES location(id);


--
-- Name: fk89eece472fde5d48; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY stock_calendar
    ADD CONSTRAINT fk89eece472fde5d48 FOREIGN KEY (ticket_type_fk) REFERENCES ticket_type(id);


--
-- Name: fk89eece4755ecafb9; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY stock_calendar
    ADD CONSTRAINT fk89eece4755ecafb9 FOREIGN KEY (product_fk) REFERENCES product(id);


--
-- Name: fk8aabbaa31a24c8f; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY product2_resource
    ADD CONSTRAINT fk8aabbaa31a24c8f FOREIGN KEY (resource_fk) REFERENCES xresource(id);


--
-- Name: fk8aabbaa55ecafb9; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY product2_resource
    ADD CONSTRAINT fk8aabbaa55ecafb9 FOREIGN KEY (product_fk) REFERENCES product(id);


--
-- Name: fk929693a0aa1ce819; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY b_o_ticket_type
    ADD CONSTRAINT fk929693a0aa1ce819 FOREIGN KEY (b_o_product_fk) REFERENCES b_o_product(id);


--
-- Name: fk9cbd90e89adc15ef; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY b_o_cart_item_b_o_product
    ADD CONSTRAINT fk9cbd90e89adc15ef FOREIGN KEY (boproduct_id) REFERENCES b_o_product(id);


--
-- Name: fk9cbd90e8d5bc215e; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY b_o_cart_item_b_o_product
    ADD CONSTRAINT fk9cbd90e8d5bc215e FOREIGN KEY (b_o_products_fk) REFERENCES b_o_cart_item(id);


--
-- Name: fk9dbee0f75b0c66e5; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY coupon_category
    ADD CONSTRAINT fk9dbee0f75b0c66e5 FOREIGN KEY (category_id) REFERENCES category(id);


--
-- Name: fk9dbee0f7e2497a99; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY coupon_category
    ADD CONSTRAINT fk9dbee0f7e2497a99 FOREIGN KEY (categories_fk) REFERENCES coupon(id);


--
-- Name: fka71cac4a4347ceaf; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY product_tag
    ADD CONSTRAINT fka71cac4a4347ceaf FOREIGN KEY (tag_id) REFERENCES tag(id);


--
-- Name: fka71cac4a912a004f; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY product_tag
    ADD CONSTRAINT fka71cac4a912a004f FOREIGN KEY (tags_fk) REFERENCES product(id);


--
-- Name: fkaf42d826602dcf9; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY coupon
    ADD CONSTRAINT fkaf42d826602dcf9 FOREIGN KEY (company_fk) REFERENCES company(id);


--
-- Name: fkb08335669891ca3e; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY country_admin
    ADD CONSTRAINT fkb08335669891ca3e FOREIGN KEY (parent_fk) REFERENCES country_admin(id);


--
-- Name: fkb0833566fc545a59; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY country_admin
    ADD CONSTRAINT fkb0833566fc545a59 FOREIGN KEY (country_fk) REFERENCES country(code);


--
-- Name: fkb2dabddc602dcf9; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY es_env
    ADD CONSTRAINT fkb2dabddc602dcf9 FOREIGN KEY (company_fk) REFERENCES company(id);


--
-- Name: fkb8bb7c45284d93f9; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY variation_value
    ADD CONSTRAINT fkb8bb7c45284d93f9 FOREIGN KEY (variation_fk) REFERENCES variation(id);


--
-- Name: fkb9d38a2d4be157d4; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY account
    ADD CONSTRAINT fkb9d38a2d4be157d4 FOREIGN KEY (location_fk) REFERENCES location(id);


--
-- Name: fkb9d38a2d602dcf9; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY account
    ADD CONSTRAINT fkb9d38a2d602dcf9 FOREIGN KEY (company_fk) REFERENCES company(id);


--
-- Name: fkba64dee855ecafb9; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY feature_value
    ADD CONSTRAINT fkba64dee855ecafb9 FOREIGN KEY (product_fk) REFERENCES product(id);


--
-- Name: fkba64dee8bc891e59; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY feature_value
    ADD CONSTRAINT fkba64dee8bc891e59 FOREIGN KEY (feature_fk) REFERENCES feature(id);


--
-- Name: fkbd40d538d411c10f; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY role_permission
    ADD CONSTRAINT fkbd40d538d411c10f FOREIGN KEY (role_fk) REFERENCES xrole(id);


--
-- Name: fkbd40d538f3e69a2f; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY role_permission
    ADD CONSTRAINT fkbd40d538f3e69a2f FOREIGN KEY (permission_fk) REFERENCES permission(id);


--
-- Name: fkc1ac11f42fde5d9e; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY coupon_ticket_type
    ADD CONSTRAINT fkc1ac11f42fde5d9e FOREIGN KEY (ticket_type_id) REFERENCES ticket_type(id);


--
-- Name: fkc1ac11f4504e2ef; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY coupon_ticket_type
    ADD CONSTRAINT fkc1ac11f4504e2ef FOREIGN KEY (ticket_types_fk) REFERENCES coupon(id);


--
-- Name: fkc5a27af655ecafb9; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY feature
    ADD CONSTRAINT fkc5a27af655ecafb9 FOREIGN KEY (product_fk) REFERENCES product(id);


--
-- Name: fkc5a27af65b0c668f; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY feature
    ADD CONSTRAINT fkc5a27af65b0c668f FOREIGN KEY (category_fk) REFERENCES category(id);


--
-- Name: fkc7d137c9793c84ef; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY user_property
    ADD CONSTRAINT fkc7d137c9793c84ef FOREIGN KEY (user_fk) REFERENCES account(id);


--
-- Name: fkc8b7cb8dbba1b1f9; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY brand_property
    ADD CONSTRAINT fkc8b7cb8dbba1b1f9 FOREIGN KEY (brand_fk) REFERENCES brand(id);


--
-- Name: fkc9ff4f7f8201a451; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY seller
    ADD CONSTRAINT fkc9ff4f7f8201a451 FOREIGN KEY (id) REFERENCES account(id);


--
-- Name: fkcc6a43bd3e1049d3; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY seller_company
    ADD CONSTRAINT fkcc6a43bd3e1049d3 FOREIGN KEY (companies_fk) REFERENCES seller(id);


--
-- Name: fkcc6a43bd602dd4f; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY seller_company
    ADD CONSTRAINT fkcc6a43bd602dd4f FOREIGN KEY (company_id) REFERENCES company(id);


--
-- Name: fkcd71f39b3f4326a2; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY consumption
    ADD CONSTRAINT fkcd71f39b3f4326a2 FOREIGN KEY (b_o_ticket_type_fk) REFERENCES b_o_ticket_type(id);


--
-- Name: fkd2ac68a1602dcf9; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY xcatalog
    ADD CONSTRAINT fkd2ac68a1602dcf9 FOREIGN KEY (company_fk) REFERENCES company(id);


--
-- Name: fkdbcb8f57602dcf9; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY company_property
    ADD CONSTRAINT fkdbcb8f57602dcf9 FOREIGN KEY (company_fk) REFERENCES company(id);


--
-- Name: fke0e95c5c722ba4bd; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY account_xrole
    ADD CONSTRAINT fke0e95c5c722ba4bd FOREIGN KEY (roles_fk) REFERENCES account(id);


--
-- Name: fke0e95c5cd411c165; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY account_xrole
    ADD CONSTRAINT fke0e95c5cd411c165 FOREIGN KEY (role_id) REFERENCES xrole(id);


--
-- Name: fked8dccef1ee8688f; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY product
    ADD CONSTRAINT fked8dccef1ee8688f FOREIGN KEY (shipping_fk) REFERENCES shipping(id);


--
-- Name: fked8dccef5b0c668f; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY product
    ADD CONSTRAINT fked8dccef5b0c668f FOREIGN KEY (category_fk) REFERENCES category(id);


--
-- Name: fked8dccef602dcf9; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY product
    ADD CONSTRAINT fked8dccef602dcf9 FOREIGN KEY (company_fk) REFERENCES company(id);


--
-- Name: fked8dccef9eca6f9; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY product
    ADD CONSTRAINT fked8dccef9eca6f9 FOREIGN KEY (ibeacon_fk) REFERENCES ibeacon(id);


--
-- Name: fked8dccefa99249af; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY product
    ADD CONSTRAINT fked8dccefa99249af FOREIGN KEY (seller_fk) REFERENCES seller(id);


--
-- Name: fked8dccefbba1b1f9; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY product
    ADD CONSTRAINT fked8dccefbba1b1f9 FOREIGN KEY (brand_fk) REFERENCES brand(id);


--
-- Name: fked8dccefbf86fc94; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY product
    ADD CONSTRAINT fked8dccefbf86fc94 FOREIGN KEY (poi_fk) REFERENCES location(id);


--
-- Name: fked8dcceff333cfd0; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY product
    ADD CONSTRAINT fked8dcceff333cfd0 FOREIGN KEY (tax_rate_fk) REFERENCES tax_rate(id);


--
-- Name: fkef7a58f4602dcf9; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY tax_rate
    ADD CONSTRAINT fkef7a58f4602dcf9 FOREIGN KEY (company_fk) REFERENCES company(id);


--
-- Name: fkf25af5d63c5eeb6a; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY b_o_returned_item
    ADD CONSTRAINT fkf25af5d63c5eeb6a FOREIGN KEY (b_o_cart_item_fk) REFERENCES b_o_cart_item(id);


--
-- Name: fkf25af5d6cd96dbaf; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY b_o_returned_item
    ADD CONSTRAINT fkf25af5d6cd96dbaf FOREIGN KEY (b_o_return_fk) REFERENCES b_o_return(id);


--
-- Name: fkf3469e3ff0cf6b53; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY google_variation_value
    ADD CONSTRAINT fkf3469e3ff0cf6b53 FOREIGN KEY (type_fk) REFERENCES google_variation_type(id);


--
-- Name: fkf581d04555ecafb9; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY intra_day_period
    ADD CONSTRAINT fkf581d04555ecafb9 FOREIGN KEY (product_fk) REFERENCES product(id);


--
-- Name: fkfb1da2135b0c668f; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY variation
    ADD CONSTRAINT fkfb1da2135b0c668f FOREIGN KEY (category_fk) REFERENCES category(id);


--
-- Name: fkfde32e3d83966c6f; Type: FK CONSTRAINT; Schema: public; Owner: mogobiz
--

ALTER TABLE ONLY b_o_return
    ADD CONSTRAINT fkfde32e3d83966c6f FOREIGN KEY (b_o_cart_fk) REFERENCES b_o_cart(id);


--
-- Name: public; Type: ACL; Schema: -; Owner: sdi
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM sdi;
GRANT ALL ON SCHEMA public TO sdi;
GRANT ALL ON SCHEMA public TO PUBLIC;


--
-- PostgreSQL database dump complete
--

