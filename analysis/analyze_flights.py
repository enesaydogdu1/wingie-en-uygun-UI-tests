import argparse, os, glob, csv, math
from datetime import datetime
from collections import defaultdict
import pandas as pd
import numpy as np
import matplotlib.pyplot as plt

# Not: Kurallar gereği seaborn yok; renk seti verilmez.

def parse_price(p):
    try:
        return int(''.join(ch for ch in p if ch.isdigit()))
    except Exception:
        return np.nan

def parse_time(t):
    try:
        return datetime.strptime(t.strip()[:5], "%H:%M").time()
    except Exception:
        return None

def load_csvs(csv_dir):
    files = glob.glob(os.path.join(csv_dir, "*.csv"))
    frames = []
    for f in files:
        with open(f, newline='', encoding='utf-8') as fh:
            reader = csv.DictReader(fh)
            rows = list(reader)
        df = pd.DataFrame(rows)
        if df.empty: 
            continue
        df['price_num'] = df['price'].apply(parse_price)
        df['airline'] = df['airline'].fillna('')
        df['dep_time_obj'] = df['departure_time'].apply(parse_time)
        df['dep_hour'] = df['dep_time_obj'].apply(lambda x: x.hour if x else np.nan)
        frames.append(df)
    return pd.concat(frames, ignore_index=True) if frames else pd.DataFrame()

def stats_by_airline(df, outdir):
    g = df.groupby('airline')['price_num'].agg(['min','max','mean']).sort_values('mean')
    g.to_csv(os.path.join(outdir, 'airline_price_stats.csv'))
    # Bar chart
    plt.figure()
    g['mean'].plot(kind='bar')
    plt.title('Havayoluna Göre Ortalama Fiyat')
    plt.ylabel('Fiyat')
    plt.tight_layout()
    plt.savefig(os.path.join(outdir, 'airline_mean_price.png'))
    plt.close()
    return g

def heatmap_by_hour(df, outdir):
    pivot = df.pivot_table(values='price_num', index='airline', columns='dep_hour', aggfunc='mean')
    pivot = pivot.sort_index()
    # Heatmap w/o seaborn: use imshow
    plt.figure()
    plt.imshow(pivot.values, aspect='auto')
    plt.xticks(ticks=range(pivot.shape[1]), labels=list(pivot.columns))
    plt.yticks(ticks=range(pivot.shape[0]), labels=list(pivot.index))
    plt.title('Saat Bazlı Ortalama Fiyat Isı Haritası')
    plt.xlabel('Kalkış Saati (Saat)')
    plt.colorbar(label='Ortalama Fiyat')
    plt.tight_layout()
    plt.savefig(os.path.join(outdir, 'hourly_heatmap.png'))
    plt.close()
    pivot.to_csv(os.path.join(outdir, 'hourly_heatmap_data.csv'))
    return pivot

def pick_cost_effective(df, outdir):
    # Basit bir skor: price z-score + (duration_minutes z-score) -> en düşük skor en iyi
    # duration parse etmeye çalış
    def dur_to_min(s):
        if not isinstance(s, str): return np.nan
        nums = [int(x) for x in s.replace('sa','h').replace('dk','m').replace(' ', '').replace('.', '').split() if x.isdigit()]
        # fallback: HH:MM formatı?
        try:
            if ':' in s:
                hh, mm = s.split(':')[:2]
                return int(hh)*60 + int(mm)
        except Exception:
            pass
        # kaba dönüşüm (ilk sayı saat, ikinci dakika varsayımı)
        if len(nums) == 0: return np.nan
        if len(nums) == 1: return nums[0]*60
        return nums[0]*60 + nums[1]

    df['duration_min'] = df['duration'].apply(dur_to_min)
    filter_df = df[['airline','price_num','duration_min','departure_time','arrival_time','stops']].dropna()
    if filter_df.empty:
        return pd.DataFrame()

    z_price = (filter_df['price_num'] - filter_df['price_num'].mean())/filter_df['price_num'].std(ddof=0)
    z_dur = (filter_df['duration_min'] - filter_df['duration_min'].mean())/filter_df['duration_min'].std(ddof=0)
    filter_df = filter_df.assign(score = z_price + z_dur).sort_values('score')
    top = filter_df.head(10)
    top.to_csv(os.path.join(outdir, 'top_cost_effective.csv'), index=False)
    return top

def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--csv-dir', required=True)
    ap.add_argument('--out', default='analysis/output')
    args = ap.parse_args()
    os.makedirs(args.out, exist_ok=True)

    df = load_csvs(args.csv_dir)
    if df.empty:
        print("CSV bulunamadı veya veri boş.")
        return

    g = stats_by_airline(df, args.out)
    heatmap_by_hour(df, args.out)
    pick_cost_effective(df, args.out)
    print("Analiz tamamlandı. Çıktılar:", args.out)

if __name__ == "__main__":
    main()
