--
-- PostgreSQL database dump
--

\restrict yBl6lR9ZPk0WQN6wscuQlJj4bwqBANJqyrBAquWyusSyJXjN0oe5jccbqL84rHT

-- Dumped from database version 18.3 (Homebrew)
-- Dumped by pg_dump version 18.0

-- Started on 2026-04-13 17:32:47 EAT

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- TOC entry 220 (class 1259 OID 17174)
-- Name: products; Type: TABLE; Schema: public; Owner: inventory
--

CREATE TABLE public.products (
    id integer NOT NULL,
    name character varying(100) NOT NULL,
    category character varying(50) NOT NULL,
    quantity integer DEFAULT 0 NOT NULL,
    price numeric(10,2) NOT NULL,
    expiry_date date,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.products OWNER TO inventory;

--
-- TOC entry 219 (class 1259 OID 17173)
-- Name: products_id_seq; Type: SEQUENCE; Schema: public; Owner: inventory
--

CREATE SEQUENCE public.products_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.products_id_seq OWNER TO inventory;

--
-- TOC entry 3748 (class 0 OID 0)
-- Dependencies: 219
-- Name: products_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: inventory
--

ALTER SEQUENCE public.products_id_seq OWNED BY public.products.id;


--
-- TOC entry 3588 (class 2604 OID 17177)
-- Name: products id; Type: DEFAULT; Schema: public; Owner: inventory
--

ALTER TABLE ONLY public.products ALTER COLUMN id SET DEFAULT nextval('public.products_id_seq'::regclass);


--
-- TOC entry 3741 (class 0 OID 17174)
-- Dependencies: 220
-- Data for Name: products; Type: TABLE DATA; Schema: public; Owner: inventory
--

COPY public.products (id, name, category, quantity, price, expiry_date, created_at) FROM stdin;
2	Sliced Bread	Perishable	30	65.00	2025-04-15	2026-04-11 19:56:05.173612
4	Cheddar Cheese	Perishable	20	350.00	2025-05-01	2026-04-11 19:56:05.173612
7	Sugar	Non-Perishable	0	160.00	\N	2026-04-11 19:56:05.173612
8	Laundry Soap	Non-Perishable	45	55.00	\N	2026-04-11 19:56:05.173612
6	Cooking Oil	Non-Perishable	80	280.00	\N	2026-04-11 19:56:05.173612
9	Test Butter	Perishable	0	220.00	2025-05-15	2026-04-11 19:58:11.202008
10	Butter	Perishable	50	500.00	2026-10-11	2026-04-12 07:42:14.776909
5	Maize Flour	Non-Perishable	89	180.00	\N	2026-04-11 19:56:05.173612
3	Yoghurt	Perishable	3	85.00	2025-04-18	2026-04-11 19:56:05.173612
1	Fresh Milk	Perishable	0	120.00	2025-04-20	2026-04-11 19:56:05.173612
11	Tissue	Non-Perishable	100	15.00	\N	2026-04-13 08:43:13.070316
\.


--
-- TOC entry 3749 (class 0 OID 0)
-- Dependencies: 219
-- Name: products_id_seq; Type: SEQUENCE SET; Schema: public; Owner: inventory
--

SELECT pg_catalog.setval('public.products_id_seq', 11, true);


--
-- TOC entry 3592 (class 2606 OID 17186)
-- Name: products products_pkey; Type: CONSTRAINT; Schema: public; Owner: inventory
--

ALTER TABLE ONLY public.products
    ADD CONSTRAINT products_pkey PRIMARY KEY (id);


--
-- TOC entry 3747 (class 0 OID 0)
-- Dependencies: 5
-- Name: SCHEMA public; Type: ACL; Schema: -; Owner: pg_database_owner
--

GRANT ALL ON SCHEMA public TO inventory;


-- Completed on 2026-04-13 17:32:49 EAT

--
-- PostgreSQL database dump complete
--

\unrestrict yBl6lR9ZPk0WQN6wscuQlJj4bwqBANJqyrBAquWyusSyJXjN0oe5jccbqL84rHT

