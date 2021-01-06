$TTL 604800
@                   IN      SOA         ns.alpha.belenot.com.       root.alpha.belenot.com. 2 604800 86400 2419200 604800
@                   IN      NS          ns.alpha.belenot.com.

{% for domain_name, hosts in domain_names.items() %}
{% for host in hosts %}
{{ domain_name }}   IN  A   {{ host }}
{% endfor %}
{% endfor %}
