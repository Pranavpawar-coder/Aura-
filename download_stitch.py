import os
import json
import urllib.request

BASE_DIR = r"D:\Aura"
SCREENS_DIR = os.path.join(BASE_DIR, "screens")
ASSETS_DIR = os.path.join(BASE_DIR, "assets")

os.makedirs(SCREENS_DIR, exist_ok=True)
os.makedirs(ASSETS_DIR, exist_ok=True)

screens = [
    {
        "id": "15c4e46c80ee4b69a963c16dc391ae1d",
        "file": "home.html",
        "title": "AURA - Home",
        "url": "https://contribution.usercontent.google.com/download?c=CgthaWRhX2NvZGVmeBJ7Eh1hcHBfY29tcGFuaW9uX2dlbmVyYXRlZF9maWxlcxpaCiVodG1sXzAwMDY1YzRlMTk0OGZkODQwNGVhYWJiYTljMzZiNjAwEgsSBxDT78zv7x0YAZIBIwoKcHJvamVjdF9pZBIVQhM2ODM5MzUxNDQwNzI2MzM2NjY5&filename=&opi=89354086"
    },
    {
        "id": "eec5e2f20a034075863a8575076200c7",
        "file": "now_playing.html",
        "title": "AURA - Now Playing",
        "url": "https://contribution.usercontent.google.com/download?c=CgthaWRhX2NvZGVmeBJ7Eh1hcHBfY29tcGFuaW9uX2dlbmVyYXRlZF9maWxlcxpaCiVodG1sXzAwMDY1YzRlMTkzN2VmYWQwMWE2Mzg3NzVmMGFlOTZkEgsSBxDT78zv7x0YAZIBIwoKcHJvamVjdF9pZBIVQhM2ODM5MzUxNDQwNzI2MzM2NjY5&filename=&opi=89354086"
    },
    {
        "id": "f7916e0b3a7e494dbf9267fd9ba21cb4",
        "file": "lyrics.html",
        "title": "AURA - Live Lyrics",
        "url": "https://contribution.usercontent.google.com/download?c=CgthaWRhX2NvZGVmeBJ7Eh1hcHBfY29tcGFuaW9uX2dlbmVyYXRlZF9maWxlcxpaCiVodG1sXzAwMDY1YzRlMzEwYzc5OGIwN2M0YzUxYzcwMDY0ZjI4EgsSBxDT78zv7x0YAZIBIwoKcHJvamVjdF9pZBIVQhM2ODM5MzUxNDQwNzI2MzM2NjY5&filename=&opi=89354086"
    },
    {
        "id": "1ede7fe349404e4ea7763afd60decf30",
        "file": "search.html",
        "title": "AURA - Search",
        "url": "https://contribution.usercontent.google.com/download?c=CgthaWRhX2NvZGVmeBJ7Eh1hcHBfY29tcGFuaW9uX2dlbmVyYXRlZF9maWxlcxpaCiVodG1sXzAwMDY1YzRlMTk0MGM4NDMwMWE2MDM3MDM4MDdmMmIyEgsSBxDT78zv7x0YAZIBIwoKcHJvamVjdF9pZBIVQhM2ODM5MzUxNDQwNzI2MzM2NjY5&filename=&opi=89354086"
    },
    {
        "id": "f47686a65a4b4100a957f6e84eb5d163",
        "file": "library.html",
        "title": "AURA - Library",
        "url": "https://contribution.usercontent.google.com/download?c=CgthaWRhX2NvZGVmeBJ7Eh1hcHBfY29tcGFuaW9uX2dlbmVyYXRlZF9maWxlcxpaCiVodG1sXzAwMDY1YzRlMTkxNmU4YjUwMjJkNmI1NTgxMTk1MzFjEgsSBxDT78zv7x0YAZIBIwoKcHJvamVjdF9pZBIVQhM2ODM5MzUxNDQwNzI2MzM2NjY5&filename=&opi=89354086"
    },
    {
        "id": "2d552aa4e8c6421c8bcf60cbaf549d3a",
        "file": "settings.html",
        "title": "AURA - Settings",
        "url": "https://contribution.usercontent.google.com/download?c=CgthaWRhX2NvZGVmeBJ7Eh1hcHBfY29tcGFuaW9uX2dlbmVyYXRlZF9maWxlcxpaCiVodG1sXzAwMDY1YzRlMzg3ZTE4YTIwNWMyZmZhOTE2MDdiYTM2EgsSBxDT78zv7x0YAZIBIwoKcHJvamVjdF9pZBIVQhM2ODM5MzUxNDQwNzI2MzM2NjY5&filename=&opi=89354086"
    },
    {
        "id": "ce630654c4c3417684dd52cb5519578b",
        "file": "prototype_overview.html",
        "title": "AURA Audiophile Music Player",
        "url": "https://contribution.usercontent.google.com/download?c=CgthaWRhX2NvZGVmeBJ7Eh1hcHBfY29tcGFuaW9uX2dlbmVyYXRlZF9maWxlcxpaCiVodG1sXzAwMDY1YzRlNzRlZWU5MzIwMDMwM2ZlMGFiMmFlNTE5EgsSBxDT78zv7x0YAZIBIwoKcHJvamVjdF9pZBIVQhM2ODM5MzUxNDQwNzI2MzM2NjY5&filename=&opi=89354086"
    }
]

images = [
    {
        "file": "album_velvet_eclipse.png",
        "url": "https://lh3.googleusercontent.com/aida/AEtjO1WtjNKuTdsD7S5KEoYHLCpNze0idfA_kAnHXrjENn85JnlGl1rbbrQ3AxNFd7fmKF13YrWKV_Zxvknx99cGET6mVesLesvLTpp4sf9mBwYU-dr9TNBQikPcYlVNFtMykx8QBrfVxA3PY8IP-L20X1lcfWUBjFD5YCBfZr0yXPBvSkUYTBTKcX-bLDkK-Wtevw630i2GEsb_MvnVTVOpn9OHgI3Q70yw2giURNWneuAHOVGbO3Bu51TIKUs"
    },
    {
        "file": "user_avatar.png",
        "url": "https://lh3.googleusercontent.com/aida/AEtjO1VNN1wjM5Tbx1HiOsp5FdBHC8YSWzPXEJXIQDTJsEAsdh3VnO3tL5ymqJtUBtOvAFyUtanzQz1XbiGt4CTQF8Rd0gzaq3LORWibXeSRQIAfuFw8u1O5kM-txJYtztFHdLeNCsuxaljEU9nKuY1IN0rb-FN0uTLpCMjMbMeoGZptCUtDAV_dcvAojnN2ei1BoR9klFxxpUYZJV_krVG11_v8RXssZFw8RgAALuLxHkoH6uk5L4j3VR15xew"
    },
    {
        "file": "aura_logo.png",
        "url": "https://lh3.googleusercontent.com/aida/AEtjO1XjP0cUYNDTjdc7WuXmKoimvkBH2t2DRBb4hki4X7DqPZRJ_b3XuuC5bgdDqimiAsm2xkJUciYXEW4KJswul_OaMdhK_Ku98xRbn5KPKJDI_62I4_27lCZmIT7EvAtRGoGRDS_d0dQSW4319V6kcZHiPfc3qoWDSHBjotECgVr2CmCynamnPk1q0cAc8PQJa6rubHPnLKzRYrclNgg59arwvNMbWsza0wr4bVDXnvqN3wcQEvkRcymaLI8"
    },
    {
        "file": "album_solar_flare.png",
        "url": "https://lh3.googleusercontent.com/aida/AEtjO1Xz4O_6tOTpIzlI5NKOuhDAzg8BSoal67haGFQyeh1Y_o1ZAifLtfI-paIORZJPjsuqnxUs9Slx4PAHm_oBQesvrC8CCvv-Z13gHDmi3bpYiaMCXkB1Se3uvVWnYild5GA-LDk-c0rmcoplUh_K2Bs6mykzL1QptK0EfTCazcnq8vTQbQ3h1JLupBpmbbwZ2uKg_0HJxxy6XpYFrq1Mp6yuFJMXkEw_qYt48gPOjuUquUHaWGtj4XEf39c"
    },
    {
        "file": "album_resonance.png",
        "url": "https://lh3.googleusercontent.com/aida/AEtjO1WTi1b9IJAPUlP608q5X0a0NPISjyTqXKrDhmpDPiq9c49BsGTmqI0EuiMm4DOEHYhJZw6oF3Ow9X-ymYcg5JhCGABS0aw7Y24PkSpzpUH1V4BvIQlPaAUjpHSySYLq_IUdvOTpi7s1DoES7jgstd7T28AOB4aUCXQiAKAKK8OG7NigyHMLD2fAh7uSMf5BCl2hEd2VNhucd3460Da3tmg_G8_Czk4hdtzPiGqTazYOvvAtfhI3NF08OoE"
    },
    {
        "file": "album_neon_horizons.png",
        "url": "https://lh3.googleusercontent.com/aida/AEtjO1VlwawDsAuD8xdf1UuDXGM9VTSkxEYYsdXEKIw4JGEu4-_9nxqbYrJ4RwwG6NppcQUTMY-rBzlzjytyLiSReVVj6-hmripjtm4e9qyRou6VSi9rh6eEdSiAzVp6uxG0A1iTrQkdPhycZYKDvHLcaeQiK9bR9fZlgTMER6B7fpg37E1a6JAdzLYCNzN57ZgQZjqFvsKwC_2e6F9SCvaLs8XUi2tHZ70fLNzXxNl4C5VdX4sYXAMlwqsqao4"
    }
]

print("Downloading screens...")
for s in screens:
    dest = os.path.join(SCREENS_DIR, s["file"])
    try:
        req = urllib.request.Request(s["url"], headers={'User-Agent': 'Mozilla/5.0'})
        with urllib.request.urlopen(req) as resp, open(dest, 'wb') as f:
            f.write(resp.read())
        print(f"Downloaded screen: {s['title']} -> {s['file']} ({os.path.getsize(dest)} bytes)")
    except Exception as e:
        print(f"Failed to download screen {s['title']}: {e}")

print("\nDownloading image assets...")
for img in images:
    dest = os.path.join(ASSETS_DIR, img["file"])
    try:
        req = urllib.request.Request(img["url"], headers={'User-Agent': 'Mozilla/5.0'})
        with urllib.request.urlopen(req) as resp, open(dest, 'wb') as f:
            f.write(resp.read())
        print(f"Downloaded asset: {img['file']} ({os.path.getsize(dest)} bytes)")
    except Exception as e:
        print(f"Failed to download asset {img['file']}: {e}")

print("\nAll downloads finished!")
