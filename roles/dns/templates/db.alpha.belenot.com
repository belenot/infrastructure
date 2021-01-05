$TTL 604800
@                   IN      SOA         ns.alpha.belenot.com.       root.alpha.belenot.com. 2 604800 86400 2419200 604800
@                   IN      NS          ns.alpha.belenot.com.

{% for host in groups['dns'] %}
ns                  IN      A           {{ host }}
{% endfor %}

{% if 'edge' in groups %}
{% for host in groups['edge'] %}
edge                IN      A           {{ host }}
{% endfor %}
{% endif %}

{% if 'aw' in groups %}
{% for host in groups['aw'] %}
aw                  IN      A           {{ host }}
{% endfor %}
{% endif %}

{% if 'nexus' in groups %}
{% for host in groups['nexus'] %}
nexus               IN      A           {{ host }}
{% endfor %}
{% endif %}


{% if 'kubernetes-master' in groups and groups['kubernetes-master'] | length %}
node-0.k8s          IN      A           {{ groups['kubernetes-master'][0] }}
{% endif %}

{% if 'kubernetes-worker' in groups %}
{% for i in range(0, groups['kubernetes-worker'] | length) %}
node-{{ i + 1 }}.k8s          IN      A           {{ groups['kubernetes-worker'][i] }}
{% endfor %}
{% endif %}